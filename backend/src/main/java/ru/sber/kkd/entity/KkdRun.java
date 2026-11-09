package ru.sber.kkd.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kkd_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KkdRun {
    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id;

    @Column(name = "run_no", nullable = false, unique = true)
    private Integer runNo;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "compared_to_run_id", length = 32)
    private String comparedToRunId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;
}
