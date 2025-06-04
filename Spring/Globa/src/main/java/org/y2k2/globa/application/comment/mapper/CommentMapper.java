package org.y2k2.globa.application.comment.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.application.comment.dto.common.CommentDto;
import org.y2k2.globa.application.comment.dto.common.ReplyDto;
import org.y2k2.globa.application.common.mapper.CustomTimestampMapper;
import org.y2k2.globa.application.common.mapper.CustomTimestampTranslator;
import org.y2k2.globa.application.common.mapper.MapCreatedTime;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.comment.projection.CommentWithHasReplyProjection;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Mapper(uses = CustomTimestampMapper.class)
public interface CommentMapper {
    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    @Mapping(source = "entity.commentId", target = "commentId")
    @Mapping(source = "entity.content", target = "content")
    @Mapping(source = "entity.userId", target = "user.userId")
    @Mapping(source = "entity.profilePath", target = "user.profile")
    @Mapping(source = "entity.name", target = "user.name")
    @Mapping(source = "entity.hasReply", target = "hasReply")
    @Mapping(source = "entity.isDeleted", target = "deleted")
    @Mapping(source = "entity.createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    CommentDto toResponseCommentDto(CommentWithHasReplyProjection entity);

    @Mapping(source = "entity.commentId", target = "commentId")
    @Mapping(source = "entity.content", target = "content")
    @Mapping(source = "entity.user.profilePath", target = "user.profile")
    @Mapping(source = "entity.user.name", target = "user.name")
    @Mapping(source = "entity.isDeleted", target = "deleted")
    @Mapping(source = "entity.createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    ReplyDto toResponseReplyDto(CommentEntity entity);

    @Mapping(source = "user", target = "user")
    @Mapping(source = "highlight", target = "highlight")
    @Mapping(source = "content", target = "content")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "deletedTime", ignore = true)
    CommentEntity toParentCommentEntity(UserEntity user, HighlightEntity highlight, String content);

    @Mapping(source = "user", target = "user")
    @Mapping(source = "highlight", target = "highlight")
    @Mapping(source = "parent", target = "parent")
    @Mapping(source = "content", target = "content")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "deletedTime", ignore = true)
    CommentEntity toChildCommentEntity(UserEntity user, HighlightEntity highlight, CommentEntity parent, String content);

    @AfterMapping
    static void handleDeletedContent(@MappingTarget CommentDto dto, CommentWithHasReplyProjection entity) {
        if (entity.getIsDeleted()) {
            dto.setContent("삭제된 댓글입니다.");
        }
    }

    @AfterMapping
    static void handleDeletedContent(@MappingTarget ReplyDto dto, CommentEntity entity) {
        if (entity.getIsDeleted()) {
            dto.setContent("삭제된 답글입니다.");
        }
    }
}
