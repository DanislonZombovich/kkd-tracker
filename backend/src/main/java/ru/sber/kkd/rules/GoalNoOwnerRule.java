package ru.sber.kkd.rules;

import org.springframework.stereotype.Component;
import ru.sber.kkd.entity.Goal;
import ru.sber.kkd.entity.KeyResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GoalNoOwnerRule implements KkdRule {
    public static final String CODE = "GOAL_NO_OWNER";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getTitle() {
        return "Цель без ответственного";
    }

    @Override
    public String getSeverity() {
        return "critical";
    }

    @Override
    public String getObjectType() {
        return "goal";
    }

    @Override
    public String getRecommendation() {
        return "Назначить ответственного за цель в модуле «Цели».";
    }

    @Override
    public List<DiscoveredIssue> evaluate(
            List<Goal> activeGoals,
            Map<String, List<KeyResult>> goalKeyResults,
            Map<String, Goal> allGoalsMap,
            LocalDate snapshotDate
    ) {
        List<DiscoveredIssue> issues = new ArrayList<>();
        for (Goal goal : activeGoals) {
            if (goal.getOwnerId() == null || goal.getOwnerId().trim().isEmpty()) {
                issues.add(DiscoveredIssue.builder()
                        .ruleCode(getCode())
                        .ruleTitle(getTitle())
                        .severity(getSeverity())
                        .objectType(getObjectType())
                        .objectId(goal.getId())
                        .goalId(goal.getId())
                        .goalTitle(goal.getTitle())
                        .unitId(goal.getUnitId())
                        .ownerId(null)
                        .ownerName(null)
                        .details("Поле «Ответственный» не заполнено")
                        .actualValue(null)
                        .expectedValue("сотрудник")
                        .recommendation(getRecommendation())
                        .build());
            }
        }
        return issues;
    }
}
