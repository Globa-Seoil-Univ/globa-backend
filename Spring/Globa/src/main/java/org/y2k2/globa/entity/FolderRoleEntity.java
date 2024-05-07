package org.y2k2.globa.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "folderRole")
@Table(name = "folder_role")
public class FolderRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    @Check(constraints = "role_id IN ('1', '2', '3')")
    private String roleId;

    @Column(name = "role_name", unique = true)
    private String roleName;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;
}
