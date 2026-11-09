package ru.sber.kkd.rules;

import org.springframework.stereotype.Component;
import ru.sber.kkd.entity.Goal;
import ru.sber.kkd.entity.KeyResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CascadeNoParentRule implements KkdRule {
    public static final String CODE = "CASCADE_NO_PARENT";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getTitle() {
        return "Цель не связана с вышестоящей";
    }

    @Override
    public String getSeverity() {
        return "major";
    }

    @Override
    public String getObjectType() {
        return "goal";
    }

    @Override
    public String getRecommendation() {
        return "Указать родительскую цель вышестоящего уровня.";
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
            // Block level goals are top-level and don't need a parent
            if ("block".equalsIgnoreCase(goal.getLevel())) {
                continue;
            }
            if (goal.getParentId() == null || goal.getParentId().trim().isEmpty() || !allGoalsMap.containsKey(goal.getParentId())) {
                issues.add(DiscoveredIssue.builder()
                        .ruleCode(getCode())
                        .ruleTitle(getTitle())
                        .severity(getSeverity())
                        .objectType(getObjectType())
                        .objectId(goal.getId())
                        .goalId(goal.getId())
                        .goalTitle(goal.getTitle())
                        .unitId(goal.getUnitId())
                        .ownerId(goal.getOwnerId())
                        .ownerName(goal.getOwnerName())
                        .details("Родительская цель вышестоящего уровня не указана")
                        .actualValue(null)
                        .expectedValue("цель вышестоящего уровня")
                        .recommendation(getRecommendation())
                        .build());
            }
        }
        return issues;
    }
}
