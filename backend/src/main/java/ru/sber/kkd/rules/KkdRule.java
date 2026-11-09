package ru.sber.kkd.rules;

import ru.sber.kkd.entity.Goal;
import ru.sber.kkd.entity.KeyResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface KkdRule {
    String getCode();
    String getTitle();
    String getSeverity();
    String getObjectType();
    String getRecommendation();

    List<DiscoveredIssue> evaluate(
            List<Goal> activeGoals,
            Map<String, List<KeyResult>> goalKeyResults,
            Map<String, Goal> allGoalsMap,
            LocalDate snapshotDate
    );
}
