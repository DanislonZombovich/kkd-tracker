package ru.sber.kkd.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "key_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeyResult {
    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "goal_id", length = 64, nullable = false, insertable = false, updatable = false)
    private String goalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "target_value", precision = 15, scale = 2)
    private BigDecimal targetValue;

    @Column(name = "fact_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal factValue;

    @Column(name = "metric_unit", length = 32)
    private String metricUnit;

    @Column(name = "weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal weight;

    @Column(name = "progress_updated_at", nullable = false)
    private LocalDate progressUpdatedAt;
}
