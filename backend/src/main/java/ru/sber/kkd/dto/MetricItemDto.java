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
public class MetricItemDto {
    private String key;
    private String title;
    private String unit;
    private Double value;
    private Double previous;
    private Double delta;

    @JsonProperty("delta_pct")
    private Double deltaPct;

    private String trend;

    @JsonProperty("higher_is_better")
    private Boolean higherIsBetter;

    private String formula;
}
