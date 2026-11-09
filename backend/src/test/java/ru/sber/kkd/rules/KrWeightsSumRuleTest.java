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

class KrWeightsSumRuleTest {

    private final KrWeightsSumRule rule = new KrWeightsSumRule();

    @Test
    @DisplayName("Сумма весов ровно 100.00% считается валидной")
    void shouldPassWhenSumIsExactly100() {
        Goal goal = Goal.builder().id("G1").status("active").level("unit").unitId("U1").build();
        KeyResult kr1 = KeyResult.builder().id("K1").goalId("G1").weight(new BigDecimal("40.00")).build();
        KeyResult kr2 = KeyResult.builder().id("K2").goalId("G1").weight(new BigDecimal("60.00")).build();

        List<DiscoveredIssue> issues = rule.evaluate(
                List.of(goal),
                Map.of("G1", List.of(kr1, kr2)),
                Map.of("G1", goal),
                LocalDate.now()
        );

        assertTrue(issues.isEmpty());
    }

    @Test
    @DisplayName("Сумма весов с минимальной допустимой погрешностью (99.995%) не вызывает ошибку")
    void shouldPassWhenSumIsWithinTolerance() {
        Goal goal = Goal.builder().id("G1").status("active").level("unit").unitId("U1").build();
        KeyResult kr1 = KeyResult.builder().id("K1").goalId("G1").weight(new BigDecimal("33.333")).build();
        KeyResult kr2 = KeyResult.builder().id("K2").goalId("G1").weight(new BigDecimal("33.333")).build();
        KeyResult kr3 = KeyResult.builder().id("K3").goalId("G1").weight(new BigDecimal("33.334")).build();

        List<DiscoveredIssue> issues = rule.evaluate(
                List.of(goal),
                Map.of("G1", List.of(kr1, kr2, kr3)),
                Map.of("G1", goal),
                LocalDate.now()
        );

        assertTrue(issues.isEmpty());
    }

    @Test
    @DisplayName("Сумма весов 80.00% (< 100%) регистрирует существенную ошибку (major)")
    void shouldReportIssueWhenSumIsLessThan100() {
        Goal goal = Goal.builder().id("G1").title("Цель с недобором веса").status("active").level("unit").unitId("U1").build();
        KeyResult kr1 = KeyResult.builder().id("K1").goalId("G1").weight(new BigDecimal("40.00")).build();
        KeyResult kr2 = KeyResult.builder().id("K2").goalId("G1").weight(new BigDecimal("40.00")).build();

        List<DiscoveredIssue> issues = rule.evaluate(
                List.of(goal),
                Map.of("G1", List.of(kr1, kr2)),
                Map.of("G1", goal),
                LocalDate.now()
        );

        assertEquals(1, issues.size());
        DiscoveredIssue issue = issues.get(0);
        assertEquals(KrWeightsSumRule.CODE, issue.getRuleCode());
        assertEquals("major", issue.getSeverity());
        assertEquals("100", issue.getExpectedValue());
        assertEquals("80.00", issue.getActualValue());
    }

    @Test
    @DisplayName("Сумма весов 110.00% (> 100%) регистрирует ошибку")
    void shouldReportIssueWhenSumIsGreaterThan100() {
        Goal goal = Goal.builder().id("G1").title("Цель с перебором веса").status("active").level("unit").unitId("U1").build();
        KeyResult kr1 = KeyResult.builder().id("K1").goalId("G1").weight(new BigDecimal("60.00")).build();
        KeyResult kr2 = KeyResult.builder().id("K2").goalId("G1").weight(new BigDecimal("50.00")).build();

        List<DiscoveredIssue> issues = rule.evaluate(
                List.of(goal),
                Map.of("G1", List.of(kr1, kr2)),
                Map.of("G1", goal),
                LocalDate.now()
        );

        assertEquals(1, issues.size());
        assertEquals("110.00", issues.get(0).getActualValue());
    }

    @Test
    @DisplayName("Цель без ключевых результатов пропускается правилом суммирования весов (обрабатывается GOAL_NO_KR)")
    void shouldIgnoreGoalWithNoKeyResults() {
        Goal goal = Goal.builder().id("G1").status("active").level("unit").unitId("U1").build();

        List<DiscoveredIssue> issues = rule.evaluate(
                List.of(goal),
                Map.of("G1", List.of()),
                Map.of("G1", goal),
                LocalDate.now()
        );

        assertTrue(issues.isEmpty());
    }
}
