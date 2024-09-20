package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.y2k2.globa.entity.UserEntity;

import java.time.LocalDateTime;
@Getter
@AllArgsConstructor
public class FolderDto {
    private Long folderId;
    private UserEntity user;
    private String title;
    private String createdTime;
}
