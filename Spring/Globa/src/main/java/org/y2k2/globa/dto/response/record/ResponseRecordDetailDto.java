package org.y2k2.globa.dto.response.record;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.y2k2.globa.dto.response.section.ResponseSectionDto;
import org.y2k2.globa.dto.response.folder.ResponseDetailFolderDto;

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
    private ResponseDetailFolderDto folder;
    private List<ResponseSectionDto> section;
    private LocalDateTime createdTime;
}
