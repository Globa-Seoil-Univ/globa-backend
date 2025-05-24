package org.y2k2.globa.application.record.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.y2k2.globa.application.user.dto.common.UserIntroDto;

import java.util.List;

@Getter
@ToString
public class ResponseRecordSearchDto {
    private final List<RecordSearchDto> records;
    private final Long total;

    @JsonCreator
    public ResponseRecordSearchDto(
            @JsonProperty("records") List<RecordSearchDto> records,
            @JsonProperty("total") Long total
    ) {
        this.records = records;
        this.total = total;
    }

    @Getter
    @ToString
    public static class RecordSearchDto {
        private final UserIntroDto uploader;
        private final Long recordId;
        private final Long folderId;
        private final String title;
        private final String createdTime;

        @JsonCreator
        public RecordSearchDto(
                @JsonProperty("uploader") UserIntroDto uploader,
                @JsonProperty("recordId") Long recordId,
                @JsonProperty("folderId") Long folderId,
                @JsonProperty("title") String title,
                @JsonProperty("createdTime") String createdTime
        ) {
            this.uploader = uploader;
            this.recordId = recordId;
            this.folderId = folderId;
            this.title = title;
            this.createdTime = createdTime;
        }
    }
}
