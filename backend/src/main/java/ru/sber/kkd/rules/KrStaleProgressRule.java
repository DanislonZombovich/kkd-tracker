package ru.sber.kkd.rules;

import org.springframework.stereotype.Component;
import ru.sber.kkd.entity.Goal;
import ru.sber.kkd.entity.KeyResult;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class KrStaleProgressRule implements KkdRule {
    public static final String CODE = "KR_STALE_PROGRESS";
    public static final long MAX_STALE_DAYS = 45;

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getTitle() {
        return "Прогресс КР давно не обновлялся";
    }

    @Override
    public String getSeverity() {
        return "minor";
    }

    @Override
    public String getObjectType() {
        return "key_result";
    }

    @Override
    public String getRecommendation() {
        return "Обновить фактическое значение КР.";
    }

    @Override
    public List<DiscoveredIssue> evaluate(
            List<Goal> activeGoals,
            Map<String, List<KeyResult>> goalKeyResults,
            Map<String, Goal> allGoalsMap,
            LocalDate snapshotDate
    ) {
        List<DiscoveredIssue> issues = new ArrayList<>();
        LocalDate today = snapshotDate != null ? snapshotDate : LocalDate.now();

        for (Goal goal : activeGoals) {
            List<KeyResult> krs = goalKeyResults.getOrDefault(goal.getId(), List.of());
            for (KeyResult kr : krs) {
                if (kr.getProgressUpdatedAt() != null) {
                    long days = ChronoUnit.DAYS.between(kr.getProgressUpdatedAt(), today);
                    if (days > MAX_STALE_DAYS) {
                        issues.add(DiscoveredIssue.builder()
                                .ruleCode(getCode())
                                .ruleTitle(getTitle())
                                .severity(getSeverity())
                                .objectType(getObjectType())
                                .objectId(kr.getId())
                                .goalId(goal.getId())
                                .goalTitle(goal.getTitle())
                                .krId(kr.getId())
                                .krTitle(kr.getTitle())
                                .unitId(goal.getUnitId())
                                .ownerId(goal.getOwnerId())
                                .ownerName(goal.getOwnerName())
                                .details("Факт обновлялся " + days + " дн. назад")
                                .actualValue(String.valueOf(days))
                                .expectedValue("<= " + MAX_STALE_DAYS)
                                .recommendation(getRecommendation())
                                .build());
                    }
                }
            }
        }
        return issues;
    }
}
