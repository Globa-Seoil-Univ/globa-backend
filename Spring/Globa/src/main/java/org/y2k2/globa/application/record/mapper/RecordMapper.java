package org.y2k2.globa.application.record.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.application.common.mapper.CustomTimestampTranslator;
import org.y2k2.globa.application.common.mapper.MapCreatedTime;
import org.y2k2.globa.application.record.dto.request.RequestPostRecordDto;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.application.common.mapper.CustomTimestampMapper;
import org.y2k2.globa.infrastructure.persistence.record.projection.RecordSearchProjection;
import org.y2k2.globa.application.folder.dto.response.ResponseDetailFolderDto;
import org.y2k2.globa.application.record.dto.response.ResponseRecordDetailDto;
import org.y2k2.globa.application.record.dto.response.ResponseRecordDto;
import org.y2k2.globa.application.record.dto.response.ResponseRecordSearchDto;
import org.y2k2.globa.application.keyword.dto.response.ResponseKeywordDto;
import org.y2k2.globa.application.record.dto.request.RequestRecordDto;
import org.y2k2.globa.application.user.dto.common.UserIntroDto;
import org.y2k2.globa.application.section.dto.response.ResponseSectionDto;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.util.List;

@Mapper(uses = CustomTimestampMapper.class)
public interface RecordMapper {
    RecordMapper INSTANCE = Mappers.getMapper(RecordMapper.class);

    @Mapping(source = "recordEntity.recordId", target = "recordId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "path", target = "path")
    @Mapping(source = "createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    RequestRecordDto toRequestRecordDto(RecordEntity recordEntity);

    @Mapping(source = "recordEntity.recordId", target = "recordId")
    @Mapping(source = "folderId", target = "folderId")
    @Mapping(source = "recordEntity.title", target = "title")
    @Mapping(source = "recordEntity.path", target = "path")
    @Mapping(source = "keywords", target = "keywords")
    @Mapping(source = "recordEntity.createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    ResponseRecordDto toResponseRecordDto(RecordEntity recordEntity, Long folderId, List<ResponseKeywordDto> keywords);

    @Mapping(source = "record.recordId", target = "recordId")
    @Mapping(source = "record.title", target = "title")
    @Mapping(source = "record.path", target = "path")
    @Mapping(source = "record.size", target = "size")
    @Mapping(source = "folder", target = "folder")
    @Mapping(source = "sections", target = "sections")
    @Mapping(source = "record.createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    ResponseRecordDetailDto toResponseRecordDetailDto(
            RecordEntity record,
            ResponseDetailFolderDto folder,
            List<ResponseSectionDto> sections
    );

    @Mapping(source = "uploader", target = "uploader")
    @Mapping(source = "folderId", target = "folderId")
    @Mapping(source = "record.recordId", target = "recordId")
    @Mapping(source = "record.title", target = "title")
    @Mapping(source = "record.createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    ResponseRecordSearchDto.RecordSearchDto toResponseRecordSearch(Long folderId, RecordSearchProjection record, UserIntroDto uploader);

    @Mapping(source = "dto.title", target = "title")
    @Mapping(source = "dto.path", target = "path")
    @Mapping(source = "user", target = "user")
    @Mapping(source = "folder", target = "folder")
    @Mapping(source = "size", target = "size")
    @Mapping(target = "createdTime", ignore = true)
    RecordEntity toEntity(RequestPostRecordDto dto, FolderEntity folder, UserEntity user, Long size);
}
