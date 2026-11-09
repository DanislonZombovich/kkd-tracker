package ru.sber.kkd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kkd_issues")
@IdClass(KkdIssueId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KkdIssueEntity {
    @Id
    @Column(name = "run_id", length = 32, nullable = false)
    private String runId;

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "fingerprint", length = 128, nullable = false)
    private String fingerprint;

    @Column(name = "rule_code", length = 64, nullable = false)
    private String ruleCode;

    @Column(name = "severity", length = 32, nullable = false)
    private String severity;

    @Column(name = "object_type", length = 32, nullable = false)
    private String objectType;

    @Column(name = "object_id", length = 64, nullable = false)
    private String objectId;

    @Column(name = "goal_id", length = 64, nullable = false)
    private String goalId;

    @Column(name = "kr_id", length = 64)
    private String krId;

    @Column(name = "unit_id", length = 32, nullable = false)
    private String unitId;

    @Column(name = "owner_id", length = 64)
    private String ownerId;

    @Column(name = "owner_name", length = 255)
    private String ownerName;

    @Column(name = "details", length = 2000, nullable = false)
    private String details;

    @Column(name = "actual_value", length = 2000)
    private String actualValue;

    @Column(name = "expected_value", length = 2000)
    private String expectedValue;

    @Column(name = "recommendation", length = 2000, nullable = false)
    private String recommendation;

    @Column(name = "delta_status", length = 32, nullable = false)
    private String deltaStatus;

    @Column(name = "runs_in_row", nullable = false)
    private Integer runsInRow;
}
