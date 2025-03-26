package org.y2k2.globa.application.folder.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;

@Getter
@AllArgsConstructor
public class FolderDto {
    private Long folderId;
    private UserEntity user;
    private String title;
    private String createdTime;
}
