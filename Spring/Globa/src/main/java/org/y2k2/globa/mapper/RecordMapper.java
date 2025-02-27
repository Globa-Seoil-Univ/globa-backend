package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.projection.RecordSearchProjection;
import org.y2k2.globa.dto.response.folder.ResponseDetailFolderDto;
import org.y2k2.globa.dto.response.record.ResponseRecordDetailDto;
import org.y2k2.globa.dto.response.record.ResponseRecordDto;
import org.y2k2.globa.dto.response.record.ResponseRecordSearchDto;
import org.y2k2.globa.dto.response.keyword.ResponseKeywordDto;
import org.y2k2.globa.dto.request.record.RequestRecordDto;
import org.y2k2.globa.dto.common.user.UserIntroDto;
import org.y2k2.globa.dto.response.section.ResponseSectionDto;
import org.y2k2.globa.entity.RecordEntity;

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
}
