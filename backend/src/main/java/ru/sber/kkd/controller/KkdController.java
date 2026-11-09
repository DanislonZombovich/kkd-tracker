package ru.sber.kkd.controller;

import org.springframework.web.bind.annotation.*;
import ru.sber.kkd.dto.IssueItemDto;
import ru.sber.kkd.dto.KkdIssuesResponse;
import ru.sber.kkd.dto.KkdSummaryResponse;
import ru.sber.kkd.service.KkdService;

@RestController
@RequestMapping("/api/kkd")
@CrossOrigin(origins = "*")
public class KkdController {
    private final KkdService kkdService;

    public KkdController(KkdService kkdService) {
        this.kkdService = kkdService;
    }

    @GetMapping("/summary")
    public KkdSummaryResponse getSummary(
            @RequestParam(name = "run_id", defaultValue = "latest") String runId,
            @RequestParam(name = "unit_id", defaultValue = "B2B") String unitId
    ) {
        return kkdService.getSummary(runId, unitId);
    }

    @GetMapping("/issues")
    public KkdIssuesResponse listIssues(
            @RequestParam(name = "run_id", defaultValue = "latest") String runId,
            @RequestParam(name = "unit_id", defaultValue = "B2B") String unitId,
            @RequestParam(name = "rule_code", required = false) String ruleCode,
            @RequestParam(name = "severity", required = false) String severity,
            @RequestParam(name = "delta_status", required = false) String deltaStatus,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return kkdService.getIssues(runId, unitId, ruleCode, severity, deltaStatus, limit, offset);
    }

    @GetMapping("/issues/{issueId}")
    public IssueItemDto getIssue(
            @PathVariable String issueId,
            @RequestParam(name = "run_id", defaultValue = "latest") String runId
    ) {
        return kkdService.getIssueById(issueId, runId);
    }
}
