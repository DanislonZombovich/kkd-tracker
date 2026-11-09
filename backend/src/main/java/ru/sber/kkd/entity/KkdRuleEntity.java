package ru.sber.kkd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kkd_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KkdRuleEntity {
    @Id
    @Column(name = "code", length = 64, nullable = false)
    private String code;

    @Column(name = "object_type", length = 32, nullable = false)
    private String objectType;

    @Column(name = "severity", length = 32, nullable = false)
    private String severity;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "description", length = 2000, nullable = false)
    private String description;

    @Column(name = "recommendation", length = 2000, nullable = false)
    private String recommendation;
}
