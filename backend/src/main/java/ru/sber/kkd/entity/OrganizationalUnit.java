package ru.sber.kkd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organizational_units")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationalUnit {
    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "level", length = 32, nullable = false)
    private String level;

    @Column(name = "parent_id", length = 32)
    private String parentId;

    @Column(name = "depth", nullable = false)
    private Integer depth;
}
