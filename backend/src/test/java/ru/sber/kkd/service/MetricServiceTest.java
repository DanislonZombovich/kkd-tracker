package ru.sber.kkd.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.kkd.dto.MetricItemDto;
import ru.sber.kkd.entity.Goal;
import ru.sber.kkd.entity.KkdIssueEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MetricServiceTest {

    private final MetricService metricService = new MetricService();

    @Test
    @DisplayName("Корректный расчёт доли чистых целей (80% при 2 дефектных из 10)")
    void shouldCalculateCleanGoalsShareCorrectly() {
        List<Goal> goals = List.of(
                Goal.builder().id("G1").status("active").level("unit").build(),
                Goal.builder().id("G2").status("active").level("unit").build(),
                Goal.builder().id("G3").status("active").level("unit").build(),
                Goal.builder().id("G4").status("active").level("unit").build(),
                Goal.builder().id("G5").status("active").level("unit").build(),
                Goal.builder().id("G6").status("active").level("unit").build(),
                Goal.builder().id("G7").status("active").level("unit").build(),
                Goal.builder().id("G8").status("active").level("unit").build(),
                Goal.builder().id("G9").status("active").level("unit").build(),
                Goal.builder().id("G10").status("active").level("unit").build()
        );

        List<KkdIssueEntity> issues = List.of(
                KkdIssueEntity.builder().id("I1").goalId("G1").ruleCode("GOAL_NO_OWNER").severity("critical").deltaStatus("new").build(),
                KkdIssueEntity.builder().id("I2").goalId("G2").ruleCode("KR_WEIGHTS_SUM").severity("major").deltaStatus("new").build()
        );

        Map<String, Double> raw = metricService.calculateRawMetrics(goals, issues);

        assertEquals(10.0, raw.get("goals_active"));
        assertEquals(2.0, raw.get("goals_with_issues"));
        assertEquals(80.0, raw.get("clean_goals_share"));
        assertEquals(2.0, raw.get("issues_total"));
        assertEquals(1.0, raw.get("critical_issues"));
    }

    @Test
    @DisplayName("Граничный случай: подразделение без целей (TX-NEW) возвращает null для доли чистых целей")
    void shouldReturnNullForEmptyUnit() {
        Map<String, Double> raw = metricService.calculateRawMetrics(List.of(), List.of());

        assertEquals(0.0, raw.get("goals_active"));
        assertNull(raw.get("clean_goals_share"));
        assertNull(raw.get("cascade_coverage"));
        assertEquals(0.0, raw.get("issues_total"));
    }

    @Test
    @DisplayName("Первый прогон (без сравнения): previous, delta и trend строго равны null, а не 0")
    void shouldReturnNullDeltasForFirstRun() {
        List<Goal> goals = List.of(Goal.builder().id("G1").status("active").level("unit").build());
        List<KkdIssueEntity> issues = List.of();

        List<MetricItemDto> metrics = metricService.computeMetrics(
                goals, issues, null, null, false
        );

        MetricItemDto cleanShare = metrics.stream()
                .filter(m -> "clean_goals_share".equals(m.getKey()))
                .findFirst()
                .orElseThrow();

        assertEquals(100.0, cleanShare.getValue());
        assertNull(cleanShare.getPrevious());
        assertNull(cleanShare.getDelta());
        assertNull(cleanShare.getDeltaPct());
        assertNull(cleanShare.getTrend());
    }

    @Test
    @DisplayName("Оценка динамики trend: рост показателя clean_goals_share оценивается как better, а рост числа проблем — как worse")
    void shouldCorrectlyEvaluateTrend() {
        List<Goal> curGoals = List.of(Goal.builder().id("G1").status("active").level("unit").build());
        List<KkdIssueEntity> curIssues = List.of(); // 100% clean, 0 issues

        List<Goal> prevGoals = List.of(Goal.builder().id("G1").status("active").level("unit").build());
        List<KkdIssueEntity> prevIssues = List.of( // 0% clean, 1 issue
                KkdIssueEntity.builder().id("I1").goalId("G1").ruleCode("GOAL_NO_OWNER").severity("critical").deltaStatus("new").build()
        );

        List<MetricItemDto> metrics = metricService.computeMetrics(
                curGoals, curIssues, prevGoals, prevIssues, true
        );

        MetricItemDto cleanShare = metrics.stream().filter(m -> "clean_goals_share".equals(m.getKey())).findFirst().orElseThrow();
        assertEquals(100.0, cleanShare.getValue());
        assertEquals(0.0, cleanShare.getPrevious());
        assertEquals(100.0, cleanShare.getDelta());
        assertEquals("better", cleanShare.getTrend()); // Рост доли чистых целей — это better!

        MetricItemDto issuesTotal = metrics.stream().filter(m -> "issues_total".equals(m.getKey())).findFirst().orElseThrow();
        assertEquals(0.0, issuesTotal.getValue());
        assertEquals(1.0, issuesTotal.getPrevious());
        assertEquals(-1.0, issuesTotal.getDelta());
        assertEquals("better", issuesTotal.getTrend()); // Снижение числа проблем — это better!
    }
}
