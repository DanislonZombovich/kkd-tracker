package ru.sber.kkd.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.sber.kkd.dto.RunsResponse;
import ru.sber.kkd.entity.Goal;
import ru.sber.kkd.entity.KkdIssueEntity;
import ru.sber.kkd.entity.KkdRun;
import ru.sber.kkd.exception.KkdException;
import ru.sber.kkd.repository.GoalRepository;
import ru.sber.kkd.repository.KkdIssueRepository;
import ru.sber.kkd.repository.KkdRunRepository;

import java.util.List;

@RestController
@RequestMapping("/api/runs")
@CrossOrigin(origins = "*")
public class RunController {
    private final KkdRunRepository runRepository;
    private final GoalRepository goalRepository;
    private final KkdIssueRepository issueRepository;

    public RunController(
            KkdRunRepository runRepository,
            GoalRepository goalRepository,
            KkdIssueRepository issueRepository
    ) {
        this.runRepository = runRepository;
        this.goalRepository = goalRepository;
        this.issueRepository = issueRepository;
    }

    @GetMapping
    public RunsResponse listRuns(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        if (limit < 1 || limit > 100) {
            throw KkdException.invalidParam("Параметр limit должен быть от 1 до 100");
        }
        if (offset < 0) {
            throw KkdException.invalidParam("Параметр offset не может быть отрицательным");
        }

        int pageNumber = offset / limit;
        PageRequest pageRequest = PageRequest.of(pageNumber, limit);
        Page<KkdRun> page = runRepository.findAllByOrderByRunNoDesc(pageRequest);

        List<RunsResponse.RunItemDto> items = page.getContent().stream()
                .map(this::toItemDto)
                .toList();

        return RunsResponse.builder()
                .items(items)
                .total(page.getTotalElements())
                .limit(limit)
                .offset(offset)
                .build();
    }

    @GetMapping("/{runId}")
    public RunsResponse.RunItemDto getRun(@PathVariable String runId) {
        KkdRun run = runRepository.findById(runId)
                .orElseThrow(() -> KkdException.notFound("RUN_NOT_FOUND", "Прогон " + runId + " не найден"));
        return toItemDto(run);
    }

    private RunsResponse.RunItemDto toItemDto(KkdRun run) {
        List<Goal> goals = goalRepository.findByStatus("active");
        List<KkdIssueEntity> issues = issueRepository.findByRunId(run.getId());

        int critical = 0;
        int activeIssues = 0;
        for (KkdIssueEntity i : issues) {
            if (!"resolved".equalsIgnoreCase(i.getDeltaStatus())) {
                activeIssues++;
                if ("critical".equalsIgnoreCase(i.getSeverity())) {
                    critical++;
                }
            }
        }

        return RunsResponse.RunItemDto.builder()
                .id(run.getId())
                .runNo(run.getRunNo())
                .status(run.getStatus())
                .snapshotDate(run.getSnapshotDate())
                .comparedToRunId(run.getComparedToRunId())
                .startedAt(run.getStartedAt())
                .finishedAt(run.getFinishedAt())
                .goalsTotal(goals.size())
                .issuesTotal(activeIssues)
                .criticalIssues(critical)
                .build();
    }
}
