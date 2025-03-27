package org.y2k2.globa.infrastructure.persistence.role.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "role")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", columnDefinition = "SMALLINT")
    private Integer roleId;

    @Column(name = "name", nullable = false, length = 10)
    private String name;
}
