package ru.sber.kkd.service;

import org.springframework.stereotype.Service;
import ru.sber.kkd.dto.MetricItemDto;
import ru.sber.kkd.entity.Goal;
import ru.sber.kkd.entity.KkdIssueEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class MetricService {
    private static final Set<String> CASCADE_RULES = Set.of(
            "CASCADE_NO_PARENT", "CASCADE_WRONG_LEVEL", "CASCADE_PARENT_INACTIVE"
    );

    public List<MetricItemDto> computeMetrics(
            List<Goal> currentGoals,
            List<KkdIssueEntity> currentIssues,
            List<Goal> previousGoals,
            List<KkdIssueEntity> previousIssues,
            boolean hasComparison
    ) {
        Map<String, Double> cur = calculateRawMetrics(currentGoals, currentIssues);
        Map<String, Double> prev = hasComparison ? calculateRawMetrics(previousGoals, previousIssues) : null;

        List<MetricDefinition> defs = List.of(
                new MetricDefinition("clean_goals_share", "Доля корректных целей", "%", true,
                        "активные цели без проблем ÷ все активные цели × 100"),
                new MetricDefinition("cascade_coverage", "Покрытие каскадом", "%", true,
                        "цели с корректной связью с вышестоящей ÷ все не-корневые цели × 100"),
                new MetricDefinition("issues_total", "Найдено проблем", "шт.", false,
                        "общее количество нарушений правил ККД"),
                new MetricDefinition("critical_issues", "Критичные проблемы", "шт.", false,
                        "количество нарушений с severity = critical"),
                new MetricDefinition("goals_active", "Активные цели", "шт.", null,
                        "количество целей в статусе active"),
                new MetricDefinition("goals_with_issues", "Цели с проблемами", "шт.", false,
                        "активные цели, у которых есть хотя бы одна проблема")
        );

        List<MetricItemDto> result = new ArrayList<>();
        for (MetricDefinition def : defs) {
            Double val = cur.get(def.key);
            Double prVal = prev != null ? prev.get(def.key) : null;

            Double delta = null;
            Double deltaPct = null;
            String trend = null;

            if (val != null && prVal != null) {
                delta = round(val - prVal, 2);
                if (Math.abs(prVal) > 0.0001) {
                    deltaPct = round((delta / prVal) * 100.0, 1);
                }
                if (Math.abs(delta) < 0.0001) {
                    trend = "same";
                } else if (def.higherIsBetter != null) {
                    boolean improved = def.higherIsBetter ? delta > 0 : delta < 0;
                    trend = improved ? "better" : "worse";
                }
            }

            result.add(MetricItemDto.builder()
                    .key(def.key)
                    .title(def.title)
                    .unit(def.unit)
                    .value(val)
                    .previous(prVal)
                    .delta(delta)
                    .deltaPct(deltaPct)
                    .trend(trend)
                    .higherIsBetter(def.higherIsBetter)
                    .formula(def.formula)
                    .build());
        }
        return result;
    }

    public Map<String, Double> calculateRawMetrics(List<Goal> goals, List<KkdIssueEntity> issues) {
        List<Goal> activeGoals = goals != null ? goals.stream()
                .filter(g -> "active".equalsIgnoreCase(g.getStatus()))
                .toList() : List.of();

        int totalActive = activeGoals.size();
        Set<String> activeGoalIds = new HashSet<>();
        for (Goal g : activeGoals) {
            activeGoalIds.add(g.getId());
        }

        List<KkdIssueEntity> activeIssues = issues != null ? issues.stream()
                .filter(i -> activeGoalIds.contains(i.getGoalId()))
                .toList() : List.of();

        Set<String> defectiveGoalIds = new HashSet<>();
        Set<String> cascadeDefectGoalIds = new HashSet<>();
        int criticalCount = 0;

        for (KkdIssueEntity issue : activeIssues) {
            defectiveGoalIds.add(issue.getGoalId());
            if (CASCADE_RULES.contains(issue.getRuleCode())) {
                cascadeDefectGoalIds.add(issue.getGoalId());
            }
            if ("critical".equalsIgnoreCase(issue.getSeverity())) {
                criticalCount++;
            }
        }

        List<Goal> nonBlockGoals = activeGoals.stream()
                .filter(g -> !"block".equalsIgnoreCase(g.getLevel()))
                .toList();
        int totalNonBlock = nonBlockGoals.size();

        Double cleanShare = totalActive > 0 ?
                round((double) (totalActive - defectiveGoalIds.size()) * 100.0 / totalActive, 1) : null;

        Double cascadeCoverage = totalNonBlock > 0 ?
                round((double) (totalNonBlock - cascadeDefectGoalIds.size()) * 100.0 / totalNonBlock, 1) : null;

        Map<String, Double> map = new HashMap<>();
        map.put("clean_goals_share", cleanShare);
        map.put("cascade_coverage", cascadeCoverage);
        map.put("issues_total", (double) activeIssues.size());
        map.put("critical_issues", (double) criticalCount);
        map.put("goals_active", (double) totalActive);
        map.put("goals_with_issues", (double) defectiveGoalIds.size());
        return map;
    }

    private static Double round(double value, int places) {
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    private record MetricDefinition(String key, String title, String unit, Boolean higherIsBetter, String formula) {}
}
