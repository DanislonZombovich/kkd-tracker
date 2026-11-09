package ru.sber.kkd.rules;

import org.springframework.stereotype.Component;
import ru.sber.kkd.entity.Goal;
import ru.sber.kkd.entity.KeyResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GoalNoKrRule implements KkdRule {
    public static final String CODE = "GOAL_NO_KR";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getTitle() {
        return "Цель без ключевых результатов";
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
        return "Добавить 1–5 измеримых ключевых результатов.";
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
            List<KeyResult> krs = goalKeyResults.getOrDefault(goal.getId(), List.of());
            if (krs.isEmpty()) {
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
                        .details("У активной цели 0 ключевых результатов")
                        .actualValue("0")
                        .expectedValue(">= 1")
                        .recommendation(getRecommendation())
                        .build());
            }
        }
        return issues;
    }
}
