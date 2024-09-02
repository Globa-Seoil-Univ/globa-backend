package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.Projection.RecordSearchProjection;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.entity.RecordEntity;
import org.y2k2.globa.entity.RecordSearchDto;

import java.util.List;

@Mapper(uses = CustomTimestampMapper.class)
public interface RecordMapper {
    RecordMapper INSTANCE = Mappers.getMapper(RecordMapper.class);

    @Mapping(source = "recordEntity.recordId", target = "recordId")
    @Mapping(source = "recordEntity.user.userId", target = "user.userId")
    @Mapping(source = "recordEntity.folder.folderId", target = "folder.folderId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "path", target = "path")
    @Mapping(source = "size", target = "size")
    @Mapping(source = "createdTime", target = "createdTime")
    RecordDto toRecordDto(RecordEntity recordEntity);

    @Mapping(source = "recordEntity.recordId", target = "recordId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "path", target = "path")
    @Mapping(source = "createdTime", target = "createdTime")
    RequestRecordDto toRequestRecordDto(RecordEntity recordEntity);

    @Mapping(source = "recordEntity.recordId", target = "recordId")
    @Mapping(source = "folderId", target = "folderId")
    @Mapping(source = "recordEntity.title", target = "title")
    @Mapping(source = "recordEntity.path", target = "path")
    @Mapping(source = "keywords", target = "keywords")
    @Mapping(source = "recordEntity.createdTime", target = "createdTime")
    ResponseAllRecordDto toResponseAllRecordDto(RecordEntity recordEntity, Long folderId, List<ResponseKeywordDto> keywords);

    @Mapping(source = "uploader", target = "uploader")
    @Mapping(source = "folderId", target = "folderId")
    @Mapping(source = "record.recordId", target = "recordId")
    @Mapping(source = "record.title", target = "title")
    @Mapping(source = "record.createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    RecordSearchDto toResponseRecordSearch(Long folderId, RecordSearchProjection record, UserIntroDto uploader);
}
