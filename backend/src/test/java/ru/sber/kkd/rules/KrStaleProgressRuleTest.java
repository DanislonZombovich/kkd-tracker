package ru.sber.kkd.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.kkd.entity.Goal;
import ru.sber.kkd.entity.KeyResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KrStaleProgressRuleTest {

    private final KrStaleProgressRule rule = new KrStaleProgressRule();

    @Test
    @DisplayName("Факт КР обновлялся более 45 дней назад — регистрируется замечание KR_STALE_PROGRESS")
    void shouldReportIssueWhenProgressIsStale() {
        LocalDate today = LocalDate.of(2026, 11, 1);
        LocalDate staleDate = today.minusDays(50); // 50 > 45

        Goal goal = Goal.builder()
                .id("GOAL-STALE-1")
                .title("Цель с устаревшим КР")
                .level("unit")
                .unitId("HQ-CIB")
                .ownerId("EMP-1")
                .status("active")
                .deadline(today.plusMonths(3))
                .build();

        KeyResult kr = KeyResult.builder()
                .id("KR-STALE")
                .goalId(goal.getId())
                .title("Устаревший КР")
                .targetValue(new BigDecimal("100.00"))
                .factValue(new BigDecimal("10.00"))
                .weight(new BigDecimal("100.00"))
                .progressUpdatedAt(staleDate)
                .build();

        List<DiscoveredIssue> issues = rule.evaluate(
                List.of(goal),
                Map.of(goal.getId(), List.of(kr)),
                Map.of(goal.getId(), goal),
                today
        );

        assertEquals(1, issues.size());
        DiscoveredIssue issue = issues.get(0);
        assertEquals(KrStaleProgressRule.CODE, issue.getRuleCode());
        assertEquals("minor", issue.getSeverity());
        assertEquals("50", issue.getActualValue());
    }

    @Test
    @DisplayName("Факт КР обновлялся 30 дней назад — нарушений нет (в пределах 45 дней)")
    void shouldNotReportIssueWhenProgressIsFresh() {
        LocalDate today = LocalDate.of(2026, 11, 1);
        LocalDate freshDate = today.minusDays(30);

        Goal goal = Goal.builder()
                .id("GOAL-FRESH-1")
                .title("Цель со свежим КР")
                .level("unit")
                .unitId("HQ-CIB")
                .ownerId("EMP-1")
                .status("active")
                .deadline(today.plusMonths(3))
                .build();

        KeyResult kr = KeyResult.builder()
                .id("KR-FRESH")
                .goalId(goal.getId())
                .title("Свежий КР")
                .targetValue(new BigDecimal("100.00"))
                .factValue(new BigDecimal("10.00"))
                .weight(new BigDecimal("100.00"))
                .progressUpdatedAt(freshDate)
                .build();

        List<DiscoveredIssue> issues = rule.evaluate(
                List.of(goal),
                Map.of(goal.getId(), List.of(kr)),
                Map.of(goal.getId(), goal),
                today
        );

        assertTrue(issues.isEmpty());
    }
}
