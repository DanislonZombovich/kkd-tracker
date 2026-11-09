package ru.sber.kkd.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.sber.kkd.dto.*;
import ru.sber.kkd.entity.*;
import ru.sber.kkd.exception.KkdException;
import ru.sber.kkd.repository.*;

import java.time.LocalDate;
import java.util.*;

@Service
public class KkdService {
    private final KkdRunRepository runRepository;
    private final OrganizationalUnitRepository unitRepository;
    private final GoalRepository goalRepository;
    private final KkdIssueRepository issueRepository;
    private final KkdRuleRepository ruleRepository;
    private final HierarchyService hierarchyService;
    private final MetricService metricService;
    private final DeltaService deltaService;

    public KkdService(
            KkdRunRepository runRepository,
            OrganizationalUnitRepository unitRepository,
            GoalRepository goalRepository,
            KkdIssueRepository issueRepository,
            KkdRuleRepository ruleRepository,
            HierarchyService hierarchyService,
            MetricService metricService,
            DeltaService deltaService
    ) {
        this.runRepository = runRepository;
        this.unitRepository = unitRepository;
        this.goalRepository = goalRepository;
        this.issueRepository = issueRepository;
        this.ruleRepository = ruleRepository;
        this.hierarchyService = hierarchyService;
        this.metricService = metricService;
        this.deltaService = deltaService;
    }

    public KkdRun resolveCompletedRun(String runId) {
        KkdRun run;
        if ("latest".equalsIgnoreCase(runId) || runId == null || runId.isBlank()) {
            run = runRepository.findLatestCompleted()
                    .orElseThrow(() -> KkdException.notFound("RUN_NOT_FOUND", "Нет завершенных прогонов ККД"));
        } else {
            run = runRepository.findById(runId)
                    .orElseThrow(() -> KkdException.notFound("RUN_NOT_FOUND", "Прогон " + runId + " не найден"));
        }

        if ("failed".equalsIgnoreCase(run.getStatus())) {
            throw KkdException.conflict("RUN_FAILED", "Прогон " + run.getId() + " завершился с ошибкой — результатов нет");
        }
        if ("running".equalsIgnoreCase(run.getStatus())) {
            throw KkdException.conflict("RUN_IN_PROGRESS", "Прогон " + run.getId() + " ещё выполняется");
        }
        return run;
    }

    public OrganizationalUnit resolveUnit(String unitId) {
        String id = (unitId == null || unitId.isBlank()) ? "B2B" : unitId.trim();
        return unitRepository.findById(id)
                .orElseThrow(() -> KkdException.notFound("UNIT_NOT_FOUND", "Подразделение " + id + " не найдено"));
    }

    public KkdSummaryResponse getSummary(String runId, String unitId) {
        KkdRun currentRun = resolveCompletedRun(runId);
        OrganizationalUnit unit = resolveUnit(unitId);

        KkdRun previousRun = null;
        if (currentRun.getComparedToRunId() != null) {
            previousRun = runRepository.findById(currentRun.getComparedToRunId()).orElse(null);
        }
        boolean hasComparison = (previousRun != null && "completed".equalsIgnoreCase(previousRun.getStatus()));

        Set<String> scopeUnitIds = hierarchyService.getDescendantsOrSelf(unit.getId());

        List<Goal> currentGoals = goalRepository.findByUnitIdInAndStatus(scopeUnitIds, "active");
        List<KkdIssueEntity> currentIssues = issueRepository.findByRunIdAndUnitIdIn(currentRun.getId(), scopeUnitIds);

        List<Goal> prevGoals = null;
        List<KkdIssueEntity> prevIssues = null;
        if (hasComparison) {
            prevGoals = goalRepository.findByUnitIdInAndStatus(scopeUnitIds, "active");
            prevIssues = issueRepository.findByRunIdAndUnitIdIn(previousRun.getId(), scopeUnitIds);
        }

        List<MetricItemDto> metrics = metricService.computeMetrics(
                currentGoals, currentIssues, prevGoals, prevIssues, hasComparison
        );

        List<SeveritySummaryDto> severitySummaries = computeSeveritySummary(currentIssues, prevIssues, hasComparison);
        List<RuleSummaryDto> ruleSummaries = computeRuleSummary(currentIssues, prevIssues, hasComparison);
        DeltaSummaryDto delta = deltaService.calculateDelta(currentIssues, prevIssues, hasComparison);

        return KkdSummaryResponse.builder()
                .run(KkdSummaryResponse.RunInfo.builder()
                        .id(currentRun.getId())
                        .status(currentRun.getStatus())
                        .snapshotDate(currentRun.getSnapshotDate())
                        .comparedToRunId(currentRun.getComparedToRunId())
                        .build())
                .comparedTo(hasComparison ? KkdSummaryResponse.RunInfo.builder()
                        .id(previousRun.getId())
                        .status(previousRun.getStatus())
                        .snapshotDate(previousRun.getSnapshotDate())
                        .comparedToRunId(previousRun.getComparedToRunId())
                        .build() : null)
                .unit(KkdSummaryResponse.UnitInfo.builder()
                        .id(unit.getId())
                        .name(unit.getName())
                        .level(unit.getLevel())
                        .parentId(unit.getParentId())
                        .path(hierarchyService.getUnitPath(unit.getId()))
                        .build())
                .metrics(metrics)
                .issuesBySeverity(severitySummaries)
                .issuesByRule(ruleSummaries)
                .delta(delta)
                .build();
    }

    private List<SeveritySummaryDto> computeSeveritySummary(
            List<KkdIssueEntity> curIssues,
            List<KkdIssueEntity> prevIssues,
            boolean hasComparison
    ) {
        List<String[]> sevs = List.of(
                new String[]{"critical", "Критичная"},
                new String[]{"major", "Существенная"},
                new String[]{"minor", "Незначительная"}
        );

        Map<String, Integer> curCounts = countBySeverity(curIssues);
        Map<String, Integer> prevCounts = hasComparison ? countBySeverity(prevIssues) : null;

        List<SeveritySummaryDto> result = new ArrayList<>();
        for (String[] sev : sevs) {
            String code = sev[0];
            String title = sev[1];
            int cur = curCounts.getOrDefault(code, 0);
            Integer prev = prevCounts != null ? prevCounts.getOrDefault(code, 0) : null;
            Integer delta = prev != null ? (cur - prev) : null;

            result.add(SeveritySummaryDto.builder()
                    .code(code)
                    .title(title)
                    .current(cur)
                    .previous(prev)
                    .delta(delta)
                    .build());
        }
        return result;
    }

    private Map<String, Integer> countBySeverity(List<KkdIssueEntity> issues) {
        Map<String, Integer> map = new HashMap<>();
        if (issues != null) {
            for (KkdIssueEntity i : issues) {
                if (!"resolved".equalsIgnoreCase(i.getDeltaStatus())) {
                    map.merge(i.getSeverity().toLowerCase(), 1, Integer::sum);
                }
            }
        }
        return map;
    }

    private List<RuleSummaryDto> computeRuleSummary(
            List<KkdIssueEntity> curIssues,
            List<KkdIssueEntity> prevIssues,
            boolean hasComparison
    ) {
        List<RuleSummaryDto> result = new ArrayList<>();
        List<KkdRuleEntity> rules = ruleRepository.findAll();

        Map<String, Integer> curCounts = countByRule(curIssues);
        Map<String, Integer> prevCounts = hasComparison ? countByRule(prevIssues) : null;

        for (KkdRuleEntity r : rules) {
            int cur = curCounts.getOrDefault(r.getCode(), 0);
            Integer prev = prevCounts != null ? prevCounts.getOrDefault(r.getCode(), 0) : null;
            Integer delta = prev != null ? (cur - prev) : null;

            result.add(RuleSummaryDto.builder()
                    .code(r.getCode())
                    .title(r.getTitle())
                    .severity(r.getSeverity())
                    .objectType(r.getObjectType())
                    .current(cur)
                    .previous(prev)
                    .delta(delta)
                    .build());
        }
        return result;
    }

    private Map<String, Integer> countByRule(List<KkdIssueEntity> issues) {
        Map<String, Integer> map = new HashMap<>();
        if (issues != null) {
            for (KkdIssueEntity i : issues) {
                if (!"resolved".equalsIgnoreCase(i.getDeltaStatus())) {
                    map.merge(i.getRuleCode(), 1, Integer::sum);
                }
            }
        }
        return map;
    }

    public KkdIssuesResponse getIssues(
            String runId,
            String unitId,
            String ruleCode,
            String severity,
            String deltaStatus,
            String q,
            String sort,
            int limit,
            int offset
    ) {
        if (limit < 1 || limit > 200) {
            throw KkdException.invalidParam("Параметр limit должен быть от 1 до 200");
        }
        if (offset < 0) {
            throw KkdException.invalidParam("Параметр offset не может быть отрицательным");
        }

        KkdRun run = resolveCompletedRun(runId);
        OrganizationalUnit unit = resolveUnit(unitId);
        Set<String> scopeUnitIds = hierarchyService.getDescendantsOrSelf(unit.getId());

        Set<String> ruleCodes = parseCsv(ruleCode);
        Set<String> severities = parseCsv(severity);
        Set<String> deltaStatuses = parseCsv(deltaStatus);

        // По умолчанию (если статус не указан) отображаются только активные проблемы (new, persisting).
        // Исправленные проблемы (resolved) отдаются только при явном указании delta_status=resolved.
        if (deltaStatuses == null) {
            deltaStatuses = Set.of("new", "persisting");
        }

        String qPattern = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;

        Sort sortOrder = switch (sort != null ? sort.trim().toLowerCase() : "id") {
            case "severity" -> Sort.by(Sort.Direction.ASC, "severity");
            case "rule" -> Sort.by(Sort.Direction.ASC, "ruleCode");
            case "unit" -> Sort.by(Sort.Direction.ASC, "unitId");
            case "age" -> Sort.by(Sort.Direction.DESC, "runsInRow");
            default -> Sort.by(Sort.Direction.ASC, "id");
        };

        int pageNumber = offset / limit;
        PageRequest pageRequest = PageRequest.of(pageNumber, limit, sortOrder);

        Page<KkdIssueEntity> page = issueRepository.findWithFilters(
                run.getId(),
                scopeUnitIds,
                ruleCodes,
                severities,
                deltaStatuses,
                qPattern,
                pageRequest
        );

        List<IssueItemDto> items = page.getContent().stream()
                .map(this::toDto)
                .toList();

        return KkdIssuesResponse.builder()
                .runId(run.getId())
                .items(items)
                .total(page.getTotalElements())
                .limit(limit)
                .offset(offset)
                .build();
    }

    public IssueItemDto getIssueById(String issueId, String runId) {
        KkdRun run = resolveCompletedRun(runId);
        List<KkdIssueEntity> matches = issueRepository.findAllByIssueId(issueId);
        KkdIssueEntity currentIssue = matches.stream()
                .filter(i -> i.getRunId().equalsIgnoreCase(run.getId()))
                .findFirst()
                .orElseThrow(() -> KkdException.notFound("ISSUE_NOT_FOUND",
                        "Проблема " + issueId + " не найдена в прогоне " + run.getId()));

        IssueItemDto dto = toDto(currentIssue);

        // Build history across runs
        List<KkdRun> allRuns = runRepository.findAll(Sort.by("runNo").ascending());
        List<IssueItemDto.HistoryItemDto> history = new ArrayList<>();
        for (KkdRun r : allRuns) {
            if (r.getRunNo() > run.getRunNo()) {
                break;
            }
            Boolean present = null;
            if ("completed".equalsIgnoreCase(r.getStatus())) {
                boolean isPresent = matches.stream()
                        .anyMatch(m -> m.getRunId().equalsIgnoreCase(r.getId()) && !"resolved".equalsIgnoreCase(m.getDeltaStatus()));
                present = isPresent;
            }

            history.add(IssueItemDto.HistoryItemDto.builder()
                    .runId(r.getId())
                    .snapshotDate(r.getSnapshotDate().toString())
                    .runStatus(r.getStatus())
                    .present(present)
                    .build());
        }
        dto.setHistory(history);
        return dto;
    }

    private IssueItemDto toDto(KkdIssueEntity entity) {
        Optional<Goal> goalOpt = goalRepository.findById(entity.getGoalId());
        Optional<OrganizationalUnit> unitOpt = unitRepository.findById(entity.getUnitId());

        return IssueItemDto.builder()
                .id(entity.getId())
                .fingerprint(entity.getFingerprint())
                .ruleCode(entity.getRuleCode())
                .ruleTitle(getRuleTitle(entity.getRuleCode()))
                .severity(entity.getSeverity())
                .objectType(entity.getObjectType())
                .objectId(entity.getObjectId())
                .goalId(entity.getGoalId())
                .goalTitle(goalOpt.map(Goal::getTitle).orElse(entity.getGoalId()))
                .krId(entity.getKrId())
                .krTitle(entity.getKrId() != null ? "КР (" + entity.getKrId() + ")" : null)
                .unitId(entity.getUnitId())
                .unitName(unitOpt.map(OrganizationalUnit::getName).orElse(entity.getUnitId()))
                .owner(entity.getOwnerId() != null ? IssueItemDto.OwnerDto.builder()
                        .id(entity.getOwnerId())
                        .fullName(entity.getOwnerName() != null ? entity.getOwnerName() : entity.getOwnerId())
                        .build() : null)
                .details(entity.getDetails())
                .actual(entity.getActualValue())
                .expected(entity.getExpectedValue())
                .recommendation(entity.getRecommendation())
                .deltaStatus(entity.getDeltaStatus())
                .runsInRow(entity.getRunsInRow())
                .build();
    }

    private String getRuleTitle(String code) {
        return switch (code) {
            case "GOAL_NO_OWNER" -> "Цель без ответственного";
            case "GOAL_NO_KR" -> "Цель без ключевых результатов";
            case "KR_WEIGHTS_SUM" -> "Сумма весов КР не равна 100%";
            case "CASCADE_NO_PARENT" -> "Цель не связана с вышестоящей";
            case "KR_NO_TARGET" -> "КР без целевого значения";
            case "KR_STALE_PROGRESS" -> "Прогресс КР давно не обновлялся";
            default -> code;
        };
    }

    private Set<String> parseCsv(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }
        Set<String> set = new HashSet<>();
        for (String item : val.split(",")) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                set.add(trimmed);
            }
        }
        return set.isEmpty() ? null : set;
    }
}
