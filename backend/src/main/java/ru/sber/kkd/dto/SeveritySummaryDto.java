package ru.sber.kkd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeveritySummaryDto {
    private String code;
    private String title;
    private Integer current;
    private Integer previous;
    private Integer delta;
}
