package org.y2k2.globa.application.record.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.y2k2.globa.application.user.dto.common.UserIntroDto;

import java.util.List;

@Getter
@AllArgsConstructor
public class ResponseRecordSearchDto {
    @AllArgsConstructor
    @Getter
    public static class RecordSearchDto {
        private UserIntroDto uploader;
        private Long recordId;
        private Long folderId;
        private String title;
        private String createdTime;
    }

    List<RecordSearchDto> records;
    Long total;
}
