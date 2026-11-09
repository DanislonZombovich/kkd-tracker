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
public class KkdIssuesResponse {
    @JsonProperty("run_id")
    private String runId;

    private List<IssueItemDto> items;
    private long total;
    private int limit;
    private int offset;
}
