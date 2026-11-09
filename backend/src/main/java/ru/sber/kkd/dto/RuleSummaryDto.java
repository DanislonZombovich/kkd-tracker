package ru.sber.kkd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleSummaryDto {
    private String code;
    private String title;
    private String severity;

    @JsonProperty("object_type")
    private String objectType;

    private Integer current;
    private Integer previous;
    private Integer delta;
}
