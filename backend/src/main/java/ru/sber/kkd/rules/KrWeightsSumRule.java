package ru.sber.kkd.rules;

import org.springframework.stereotype.Component;
import ru.sber.kkd.entity.Goal;
import ru.sber.kkd.entity.KeyResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class KrWeightsSumRule implements KkdRule {
    public static final String CODE = "KR_WEIGHTS_SUM";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getTitle() {
        return "Сумма весов КР не равна 100%";
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
        return "Перераспределить веса КР так, чтобы сумма была ровно 100%.";
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
                continue; // Covered by GOAL_NO_KR
            }
            BigDecimal sum = BigDecimal.ZERO;
            for (KeyResult kr : krs) {
                if (kr.getWeight() != null) {
                    sum = sum.add(kr.getWeight());
                }
            }
            BigDecimal expected = new BigDecimal("100.00");
            if (sum.subtract(expected).abs().compareTo(new BigDecimal("0.01")) > 0) {
                BigDecimal rounded = sum.setScale(2, RoundingMode.HALF_UP);
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
                        .details("Сумма весов КР = " + rounded.stripTrailingZeros().toPlainString() + "%, ожидается 100%")
                        .actualValue(rounded.toPlainString())
                        .expectedValue("100")
                        .recommendation(getRecommendation())
                        .build());
            }
        }
        return issues;
    }
}
