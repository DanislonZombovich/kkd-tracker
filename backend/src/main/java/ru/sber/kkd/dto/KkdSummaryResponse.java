package ru.sber.kkd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KkdSummaryResponse {
    private RunInfo run;

    @JsonProperty("compared_to")
    private RunInfo comparedTo;

    private UnitInfo unit;
    private List<MetricItemDto> metrics;

    @JsonProperty("issues_by_severity")
    private List<SeveritySummaryDto> issuesBySeverity;

    @JsonProperty("issues_by_rule")
    private List<RuleSummaryDto> issuesByRule;

    private DeltaSummaryDto delta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RunInfo {
        private String id;
        private String status;

        @JsonProperty("snapshot_date")
        private LocalDate snapshotDate;

        @JsonProperty("compared_to_run_id")
        private String comparedToRunId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnitInfo {
        private String id;
        private String name;
        private String level;

        @JsonProperty("parent_id")
        private String parentId;

        private List<Map<String, String>> path;
    }
}
