package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.UserEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class RequestRecordDto implements Serializable {
    private Long recordId;
    private String title;
    private String path;
    private LocalDateTime createdTime;
}
