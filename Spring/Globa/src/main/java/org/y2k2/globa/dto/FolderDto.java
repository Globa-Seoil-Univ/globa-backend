package org.y2k2.globa.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.y2k2.globa.entity.UserEntity;

import java.time.LocalDateTime;
@Getter
@AllArgsConstructor
public class FolderDto {
    private Long folderId;
    private UserEntity user;
    private String title;
    private LocalDateTime createdTime;
}
