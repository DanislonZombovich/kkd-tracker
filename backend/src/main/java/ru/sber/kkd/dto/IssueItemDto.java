package ru.sber.kkd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueItemDto {
    private String id;
    private String fingerprint;

    @JsonProperty("rule_code")
    private String ruleCode;

    @JsonProperty("rule_title")
    private String ruleTitle;

    private String severity;

    @JsonProperty("object_type")
    private String objectType;

    @JsonProperty("object_id")
    private String objectId;

    @JsonProperty("goal_id")
    private String goalId;

    @JsonProperty("goal_title")
    private String goalTitle;

    @JsonProperty("kr_id")
    private String krId;

    @JsonProperty("kr_title")
    private String krTitle;

    @JsonProperty("unit_id")
    private String unitId;

    @JsonProperty("unit_name")
    private String unitName;

    private OwnerDto owner;

    private String details;
    private String actual;
    private String expected;
    private String recommendation;

    @JsonProperty("delta_status")
    private String deltaStatus;

    @JsonProperty("runs_in_row")
    private Integer runsInRow;

    private List<HistoryItemDto> history;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerDto {
        private String id;

        @JsonProperty("full_name")
        private String fullName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryItemDto {
        @JsonProperty("run_id")
        private String runId;

        @JsonProperty("snapshot_date")
        private String snapshotDate;

        @JsonProperty("run_status")
        private String runStatus;

        private Boolean present;
    }
}
