package org.y2k2.globa.infrastructure.persistence.userrole.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Getter
@Setter
@Entity
@Table(name = "user_role")
public class UserRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_role_id", columnDefinition = "INT UNSIGNED")
    private Long userRoleId;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, unique = true, columnDefinition = "INT UNSIGNED")
    private UserEntity user;

    @OneToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "role_id", columnDefinition = "SMALLINT")
    private RoleEntity roleId;
}
