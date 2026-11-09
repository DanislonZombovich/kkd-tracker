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

class GoalNoKrRuleTest {

    private final GoalNoKrRule rule = new GoalNoKrRule();

    @Test
    @DisplayName("Активная цель без ключевых результатов регистрирует критическую ошибку GOAL_NO_KR")
    void shouldReportIssueWhenNoKeyResults() {
        Goal goal = Goal.builder()
                .id("GOAL-NO-KR-1")
                .title("Цель без КР")
                .level("unit")
                .unitId("HQ-CIB")
                .ownerId("EMP-1")
                .status("active")
                .deadline(LocalDate.now().plusMonths(3))
                .build();

        List<DiscoveredIssue> issues = rule.evaluate(
                List.of(goal),
                Map.of(),
                Map.of(goal.getId(), goal),
                LocalDate.now()
        );

        assertEquals(1, issues.size());
        DiscoveredIssue issue = issues.get(0);
        assertEquals(GoalNoKrRule.CODE, issue.getRuleCode());
        assertEquals("critical", issue.getSeverity());
        assertEquals("GOAL-NO-KR-1", issue.getObjectId());
        assertEquals("0", issue.getActualValue());
        assertEquals(">= 1", issue.getExpectedValue());
    }

    @Test
    @DisplayName("Активная цель с хотя бы одним КР не регистрирует ошибку GOAL_NO_KR")
    void shouldNotReportIssueWhenKeyResultsPresent() {
        Goal goal = Goal.builder()
                .id("GOAL-HAS-KR-1")
                .title("Цель с КР")
                .level("unit")
                .unitId("HQ-CIB")
                .ownerId("EMP-1")
                .status("active")
                .deadline(LocalDate.now().plusMonths(3))
                .build();

        KeyResult kr = KeyResult.builder()
                .id("KR-1")
                .goalId(goal.getId())
                .title("Ключевой результат 1")
                .weight(new BigDecimal("100.00"))
                .targetValue(new BigDecimal("10.00"))
                .factValue(new BigDecimal("5.00"))
                .progressUpdatedAt(LocalDate.now())
                .build();

        List<DiscoveredIssue> issues = rule.evaluate(
                List.of(goal),
                Map.of(goal.getId(), List.of(kr)),
                Map.of(goal.getId(), goal),
                LocalDate.now()
        );

        assertTrue(issues.isEmpty());
    }
}
