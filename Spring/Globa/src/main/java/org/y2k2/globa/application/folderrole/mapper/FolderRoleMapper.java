package org.y2k2.globa.application.folderrole.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.application.comment.dto.common.CommentDto;
import org.y2k2.globa.application.comment.dto.common.ReplyDto;
import org.y2k2.globa.application.comment.mapper.CommentMapper;
import org.y2k2.globa.application.common.mapper.CustomTimestampMapper;
import org.y2k2.globa.application.common.mapper.CustomTimestampTranslator;
import org.y2k2.globa.application.common.mapper.MapCreatedTime;
import org.y2k2.globa.application.folderrole.command.FolderRoleCommand;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Mapper(uses = CustomTimestampMapper.class)
public interface FolderRoleMapper {
    FolderRoleMapper INSTANCE = Mappers.getMapper(FolderRoleMapper.class);

    @Mapping(source = "command.folderRole", target = "roleName")
    FolderRoleEntity toEntity(FolderRoleCommand command);
}
