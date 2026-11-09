package ru.sber.kkd.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.kkd.entity.Goal;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CascadeNoParentRuleTest {

    private final CascadeNoParentRule rule = new CascadeNoParentRule();

    @Test
    @DisplayName("Корневая цель блока (level='block') без родительской цели НЕ является нарушением")
    void shouldAllowBlockLevelGoalWithoutParent() {
        Goal blockGoal = Goal.builder()
                .id("GOAL-BLOCK")
                .level("block")
                .status("active")
                .parentId(null)
                .unitId("B2B")
                .build();

        List<DiscoveredIssue> issues = rule.evaluate(
                List.of(blockGoal),
                Map.of(),
                Map.of("GOAL-BLOCK", blockGoal),
                LocalDate.now()
        );

        assertTrue(issues.isEmpty());
    }

    @Test
    @DisplayName("Цель уровня подразделения (unit) без родительской цели регистрирует major ошибку CASCADE_NO_PARENT")
    void shouldReportIssueForNonBlockGoalWithoutParent() {
        Goal unitGoal = Goal.builder()
                .id("GOAL-UNIT")
                .title("Оторванная от стратегии цель")
                .level("unit")
                .status("active")
                .parentId(null)
                .unitId("CIB-KEY")
                .build();

        List<DiscoveredIssue> issues = rule.evaluate(
                List.of(unitGoal),
                Map.of(),
                Map.of("GOAL-UNIT", unitGoal),
                LocalDate.now()
        );

        assertEquals(1, issues.size());
        DiscoveredIssue issue = issues.get(0);
        assertEquals(CascadeNoParentRule.CODE, issue.getRuleCode());
        assertEquals("major", issue.getSeverity());
        assertEquals("GOAL-UNIT", issue.getObjectId());
    }

    @Test
    @DisplayName("Цель подразделения с корректной родительской связью проходит проверку")
    void shouldPassWhenParentGoalExists() {
        Goal parent = Goal.builder().id("GOAL-PARENT").level("hq").status("active").unitId("HQ-CIB").build();
        Goal child = Goal.builder().id("GOAL-CHILD").level("unit").status("active").parentId("GOAL-PARENT").unitId("CIB-KEY").build();

        List<DiscoveredIssue> issues = rule.evaluate(
                List.of(child),
                Map.of(),
                Map.of("GOAL-PARENT", parent, "GOAL-CHILD", child),
                LocalDate.now()
        );

        assertTrue(issues.isEmpty());
    }
}
