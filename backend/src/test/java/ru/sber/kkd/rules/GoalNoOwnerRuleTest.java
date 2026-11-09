package ru.sber.kkd.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.kkd.entity.Goal;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GoalNoOwnerRuleTest {

    private final GoalNoOwnerRule rule = new GoalNoOwnerRule();

    @Test
    @DisplayName("Активная цель без ответственного (null) регистрирует критическую ошибку")
    void shouldReportIssueWhenOwnerIsNull() {
        Goal goal = Goal.builder()
                .id("GOAL-TEST-1")
                .title("Цель без ответственного")
                .level("hq")
                .unitId("HQ-CIB")
                .ownerId(null)
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
        assertEquals(GoalNoOwnerRule.CODE, issue.getRuleCode());
        assertEquals("critical", issue.getSeverity());
        assertEquals("goal", issue.getObjectType());
        assertEquals("GOAL-TEST-1", issue.getObjectId());
        assertEquals("GOAL_NO_OWNER:GOAL-TEST-1", issue.getFingerprint());
        assertTrue(issue.generateIssueId().startsWith("ISS-"));
    }

    @Test
    @DisplayName("Активная цель с пустым строковым значением ответственного регистрирует ошибку")
    void shouldReportIssueWhenOwnerIsBlankString() {
        Goal goal = Goal.builder()
                .id("GOAL-TEST-2")
                .title("Цель с пробелами в ответственном")
                .level("unit")
                .unitId("CIB-KEY")
                .ownerId("   ")
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
        assertEquals("GOAL-TEST-2", issues.get(0).getObjectId());
    }

    @Test
    @DisplayName("Активная цель с валидным ответственным не содержит замечаний")
    void shouldNotReportIssueWhenOwnerIsPresent() {
        Goal goal = Goal.builder()
                .id("GOAL-TEST-3")
                .title("Корректная цель")
                .level("unit")
                .unitId("CIB-KEY")
                .ownerId("EMP-123")
                .ownerName("Иванов Иван")
                .status("active")
                .deadline(LocalDate.now().plusMonths(3))
                .build();

        List<DiscoveredIssue> issues = rule.evaluate(
                List.of(goal),
                Map.of(),
                Map.of(goal.getId(), goal),
                LocalDate.now()
        );

        assertTrue(issues.isEmpty());
    }
}
