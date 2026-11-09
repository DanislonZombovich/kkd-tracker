package ru.sber.kkd.rules;

import org.springframework.stereotype.Component;
import ru.sber.kkd.entity.Goal;
import ru.sber.kkd.entity.KeyResult;

import java.time.LocalDate;
import java.util.*;

@Component
public class KkdRuleEngine {
    private final List<KkdRule> rules;

    public KkdRuleEngine(List<KkdRule> rules) {
        this.rules = rules;
    }

    public List<DiscoveredIssue> executeRules(
            List<Goal> goals,
            Map<String, List<KeyResult>> goalKeyResults,
            LocalDate snapshotDate
    ) {
        List<Goal> activeGoals = goals.stream()
                .filter(g -> "active".equalsIgnoreCase(g.getStatus()))
                .toList();

        Map<String, Goal> allGoalsMap = new HashMap<>();
        for (Goal g : goals) {
            allGoalsMap.put(g.getId(), g);
        }

        List<DiscoveredIssue> allIssues = new ArrayList<>();
        for (KkdRule rule : rules) {
            List<DiscoveredIssue> issues = rule.evaluate(activeGoals, goalKeyResults, allGoalsMap, snapshotDate);
            allIssues.addAll(issues);
        }
        return allIssues;
    }
}
