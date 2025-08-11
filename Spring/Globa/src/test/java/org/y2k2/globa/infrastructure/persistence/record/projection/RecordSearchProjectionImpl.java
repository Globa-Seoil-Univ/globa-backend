package org.y2k2.globa.infrastructure.persistence.record.projection;

import org.junit.jupiter.params.shadow.com.univocity.parsers.common.record.Record;

import java.time.LocalDateTime;

public class RecordSearchProjectionImpl implements RecordSearchProjection {
    private Long userId;
    private String name;
    private String profilePath;
    private Long recordId;
    private Long folderId;
    private String title;
    private LocalDateTime createdTime;

    public RecordSearchProjectionImpl(Long userId, String name, String profilePath, Long recordId, Long folderId, String title, LocalDateTime createdTime) {
        this.userId = userId;
        this.name = name;
        this.profilePath = profilePath;
        this.recordId = recordId;
        this.folderId = folderId;
        this.title = title;
        this.createdTime = createdTime;
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getProfilePath() {
        return profilePath;
    }

    @Override
    public Long getRecordId() {
        return recordId;
    }

    @Override
    public Long getFolderId() {
        return folderId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
}
