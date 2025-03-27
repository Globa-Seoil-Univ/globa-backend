package org.y2k2.globa.application.comment.dto.request;

import lombok.Builder;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

public record RequestCommentWithIdsDto(
        UserEntity user,
        Long folderId,
        Long recordId,
        Long sectionId,
        Long highlightId,
        Long parentId
) {
    @Builder
    public RequestCommentWithIdsDto(UserEntity user, Long folderId, Long recordId, Long sectionId, Long highlightId, Long parentId) {
        this.user = user;
        this.folderId = folderId;
        this.recordId = recordId;
        this.sectionId = sectionId;
        this.highlightId = highlightId;
        this.parentId = parentId;
    }
}
