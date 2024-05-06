package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ResponseRecordDetailDto {
    private Long recordId;
    private String title;
    private String path;
    private String size;
//    private UserEntity user;
    private ResponseDetailFolderDto folder;
    private List<ResponseSectionDto> section;
    private LocalDateTime createdTime;
}
