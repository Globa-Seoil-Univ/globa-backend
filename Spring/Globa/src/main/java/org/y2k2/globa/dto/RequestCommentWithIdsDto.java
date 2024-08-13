package org.y2k2.globa.dto;

import lombok.Getter;

@Getter
public class RequestCommentWithIdsDto {
    private long userId;
    private long folderId;
    private long recordId;
    private long sectionId;
    private long highlightId;
    private long parentId;

    public RequestCommentWithIdsDto(long userId, long folderId, long recordId, long sectionId) {
        this.userId = userId;
        this.folderId = folderId;
        this.recordId = recordId;
        this.sectionId = sectionId;
    }


    public RequestCommentWithIdsDto(long userId, long folderId, long recordId, long sectionId, long highlightId) {
        this.userId = userId;
        this.folderId = folderId;
        this.recordId = recordId;
        this.sectionId = sectionId;
        this.highlightId = highlightId;
    }

    public RequestCommentWithIdsDto(long userId, long folderId, long recordId, long sectionId, long highlightId, long parentId) {
        this.userId = userId;
        this.folderId = folderId;
        this.recordId = recordId;
        this.sectionId = sectionId;
        this.highlightId = highlightId;
        this.parentId = parentId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }

    public void setSectionId(long sectionId) {
        this.sectionId = sectionId;
    }

    public void setHighlightId(long highlightId) {
        this.highlightId = highlightId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public long getUserId() {
        return userId;
    }

    public long getFolderId() {
        return folderId;
    }

    public long getRecordId() {
        return recordId;
    }

    public long getSectionId() {
        return sectionId;
    }

    public long getHighlightId() {
        return highlightId;
    }

    public long getParentId() {
        return parentId;
    }
}
