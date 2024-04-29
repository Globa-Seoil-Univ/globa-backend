package org.y2k2.globa.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.CommentDto;
import org.y2k2.globa.dto.ReplyDto;
import org.y2k2.globa.dto.ResponseCommentDto;
import org.y2k2.globa.entity.CommentEntity;

@Mapper(uses = CustomTimestampMapper.class)
public interface CommentMapper {
    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    @Mapping(source = "entity.commentId", target = "commentId")
    @Mapping(source = "entity.content", target = "content")
    @Mapping(source = "entity.user.profilePath", target = "user.profile")
    @Mapping(source = "entity.user.name", target = "user.name")
    @Mapping(source = "entity.hasReply", target = "hasReply")
    @Mapping(source = "entity.deleted", target = "deleted")
    @Mapping(source = "entity.createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    CommentDto toResponseCommentDto(CommentEntity entity);

    @Mapping(source = "entity.commentId", target = "commentId")
    @Mapping(source = "entity.content", target = "content")
    @Mapping(source = "entity.user.profilePath", target = "user.profile")
    @Mapping(source = "entity.user.name", target = "user.name")
    @Mapping(source = "entity.deleted", target = "deleted")
    @Mapping(source = "entity.createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    ReplyDto toResponseReplyDto(CommentEntity entity);

    @AfterMapping
    static void handleDeletedContent(@MappingTarget CommentDto dto, CommentEntity entity) {
        if (entity.getDeletedTime() != null) {
            dto.setContent("");
        }
    }

    @AfterMapping
    static void handleDeletedContent(@MappingTarget ReplyDto dto, CommentEntity entity) {
        if (entity.getDeletedTime() != null) {
            dto.setContent("");
        }
    }
}
