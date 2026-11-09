package ru.sber.kkd.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {
    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "level", length = 32, nullable = false)
    private String level;

    @Column(name = "unit_id", length = 32, nullable = false)
    private String unitId;

    @Column(name = "owner_id", length = 64)
    private String ownerId;

    @Column(name = "owner_name", length = 255)
    private String ownerName;

    @Column(name = "parent_id", length = 64)
    private String parentId;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<KeyResult> keyResults = new ArrayList<>();
}
