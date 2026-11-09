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

class KrNoTargetRuleTest {

    private final KrNoTargetRule rule = new KrNoTargetRule();

    @Test
    @DisplayName("КР без целевого значения регистрирует ошибку KR_NO_TARGET")
    void shouldReportIssueWhenTargetIsNull() {
        Goal goal = Goal.builder()
                .id("GOAL-KR-1")
                .title("Цель с КР")
                .level("unit")
                .unitId("HQ-CIB")
                .ownerId("EMP-1")
                .status("active")
                .deadline(LocalDate.now().plusMonths(3))
                .build();

        KeyResult kr = KeyResult.builder()
                .id("KR-NO-TARGET")
                .goalId(goal.getId())
                .title("КР без цели")
                .targetValue(null)
                .factValue(BigDecimal.ZERO)
                .weight(new BigDecimal("100.00"))
                .progressUpdatedAt(LocalDate.now())
                .build();

        List<DiscoveredIssue> issues = rule.evaluate(
                List.of(goal),
                Map.of(goal.getId(), List.of(kr)),
                Map.of(goal.getId(), goal),
                LocalDate.now()
        );

        assertEquals(1, issues.size());
        DiscoveredIssue issue = issues.get(0);
        assertEquals(KrNoTargetRule.CODE, issue.getRuleCode());
        assertEquals("major", issue.getSeverity());
        assertEquals("key_result", issue.getObjectType());
        assertEquals("KR-NO-TARGET", issue.getObjectId());
    }

    @Test
    @DisplayName("КР с заполненным целевым значением не содержит замечаний")
    void shouldNotReportIssueWhenTargetIsPresent() {
        Goal goal = Goal.builder()
                .id("GOAL-KR-2")
                .title("Цель с нормальным КР")
                .level("unit")
                .unitId("HQ-CIB")
                .ownerId("EMP-1")
                .status("active")
                .deadline(LocalDate.now().plusMonths(3))
                .build();

        KeyResult kr = KeyResult.builder()
                .id("KR-VALID-TARGET")
                .goalId(goal.getId())
                .title("КР с целью")
                .targetValue(new BigDecimal("100.00"))
                .factValue(new BigDecimal("50.00"))
                .weight(new BigDecimal("100.00"))
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
