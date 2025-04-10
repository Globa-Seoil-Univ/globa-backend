package org.y2k2.globa.application.comment.dto.request;

import lombok.Builder;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Builder
public record RequestCommentWithIdsDto(
        Long userId,
        Long folderId,
        Long recordId,
        Long sectionId,
        Long highlightId,
        Long parentId
) {
}
