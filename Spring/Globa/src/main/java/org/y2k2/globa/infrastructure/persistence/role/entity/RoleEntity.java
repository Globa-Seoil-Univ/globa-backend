package org.y2k2.globa.infrastructure.persistence.role.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.domain.role.type.UserRole;

@Getter
@Setter
@Entity
@Table(name = "role")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", columnDefinition = "SMALLINT")
    private Integer roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, columnDefinition = "ENUM('ADMIN', 'EDITOR', 'VIEWER', 'USER')")
    private UserRole name;
}
