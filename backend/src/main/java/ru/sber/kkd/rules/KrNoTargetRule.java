package ru.sber.kkd.rules;

import org.springframework.stereotype.Component;
import ru.sber.kkd.entity.Goal;
import ru.sber.kkd.entity.KeyResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class KrNoTargetRule implements KkdRule {
    public static final String CODE = "KR_NO_TARGET";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getTitle() {
        return "КР без целевого значения";
    }

    @Override
    public String getSeverity() {
        return "major";
    }

    @Override
    public String getObjectType() {
        return "key_result";
    }

    @Override
    public String getRecommendation() {
        return "Заполнить целевое значение КР.";
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
            for (KeyResult kr : krs) {
                if (kr.getTargetValue() == null) {
                    issues.add(DiscoveredIssue.builder()
                            .ruleCode(getCode())
                            .ruleTitle(getTitle())
                            .severity(getSeverity())
                            .objectType(getObjectType())
                            .objectId(kr.getId())
                            .goalId(goal.getId())
                            .goalTitle(goal.getTitle())
                            .krId(kr.getId())
                            .krTitle(kr.getTitle())
                            .unitId(goal.getUnitId())
                            .ownerId(goal.getOwnerId())
                            .ownerName(goal.getOwnerName())
                            .details("Целевое значение не заполнено")
                            .actualValue(null)
                            .expectedValue("число")
                            .recommendation(getRecommendation())
                            .build());
                }
            }
        }
        return issues;
    }
}
