package ru.sber.kkd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunsResponse {
    private List<RunItemDto> items;
    private long total;
    private int limit;
    private int offset;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RunItemDto {
        private String id;

        @JsonProperty("run_no")
        private Integer runNo;

        private String status;

        @JsonProperty("snapshot_date")
        private LocalDate snapshotDate;

        @JsonProperty("compared_to_run_id")
        private String comparedToRunId;

        @JsonProperty("started_at")
        private LocalDateTime startedAt;

        @JsonProperty("finished_at")
        private LocalDateTime finishedAt;

        @JsonProperty("goals_total")
        private Integer goalsTotal;

        @JsonProperty("issues_total")
        private Integer issuesTotal;

        @JsonProperty("critical_issues")
        private Integer criticalIssues;
    }
}
