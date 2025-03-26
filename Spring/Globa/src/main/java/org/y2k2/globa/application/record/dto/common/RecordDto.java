package org.y2k2.globa.application.record.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.y2k2.globa.insfrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class RecordDto implements Serializable {
    private Long recordId;
    private UserEntity user;
    private FolderEntity folder;
    private String title;
    private String path;
    private String size;
    private LocalDateTime createdTime;
}
