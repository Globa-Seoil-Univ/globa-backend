package org.y2k2.globa.application.comment.usecase;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.comment.command.GetInfoForCommentCommand;
import org.y2k2.globa.application.comment.dto.common.InfoForCommentDto;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.domain.highlight.repository.HighlightRepository;
import org.y2k2.globa.domain.section.repository.SectionRepository;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

import java.util.Optional;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class GetInfoForCommentUseCaseTest {
    @InjectMocks
    private GetInfoForCommentUseCase getInfoForCommentUseCase;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private HighlightRepository highlightRepository;
    @Mock
    private FolderShareRepository folderShareRepository;

    @Test
    @DisplayName("댓글 메타 정보 조회 - 성공")
    void getInfoForComment() {
        GetInfoForCommentCommand command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(GetInfoForCommentCommand.class)
                .sample();

        SectionEntity section = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(SectionEntity.class)
                .set("sectionId", command.dto().sectionId())
                .set("record.recordId", command.dto().recordId())
                .set("record.folder.folderId", command.dto().folderId())
                .sample();

        HighlightEntity highlight = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(HighlightEntity.class)
                .set("highlightId", command.dto().highlightId())
                .set("section.sectionId", command.dto().sectionId())
                .sample();

        FolderShareEntity folderShare = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("folder.folderId", command.dto().folderId())
                .set("targetUser.userId", command.dto().userId())
                .sample();

        Mockito
                .when(sectionRepository.getSectionJoinFolderAndRecord(command.dto().sectionId(), command.dto().folderId(), command.dto().recordId()))
                .thenReturn(Optional.of(section));

        Mockito
                .when(highlightRepository.getHighlight(command.dto().sectionId(), command.dto().highlightId()))
                .thenReturn(Optional.of(highlight));

        Mockito
                .when(folderShareRepository.getShareInvitation(command.dto().folderId(), command.dto().userId()))
                .thenReturn(Optional.of(folderShare));

        InfoForCommentDto result = getInfoForCommentUseCase.execute(command);

        log.info("result = {}", result);

        Assertions
                .assertThat(result.section())
                .isEqualTo(section);

        Assertions
                .assertThat(result.highlight())
                .isEqualTo(highlight);

        Assertions
                .assertThat(result.folderShare())
                .isEqualTo(folderShare);

        Mockito
                .verify(sectionRepository, Mockito.times(1))
                .getSectionJoinFolderAndRecord(command.dto().sectionId(), command.dto().folderId(), command.dto().recordId());

        Mockito
                .verify(highlightRepository, Mockito.times(1))
                .getHighlight(command.dto().sectionId(), command.dto().highlightId());

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .getShareInvitation(command.dto().folderId(), command.dto().userId());
    }

    @Test
    @DisplayName("댓글 메타 정보 조회 - 실패 (섹션 없음)")
    void getInfoForCommentFailSectionNotFound() {
        GetInfoForCommentCommand command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(GetInfoForCommentCommand.class)
                .sample();

        Mockito
                .when(sectionRepository.getSectionJoinFolderAndRecord(command.dto().sectionId(), command.dto().folderId(), command.dto().recordId()))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> getInfoForCommentUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_SECTION);

        Mockito
                .verify(sectionRepository, Mockito.times(1))
                .getSectionJoinFolderAndRecord(command.dto().sectionId(), command.dto().folderId(), command.dto().recordId());

        Mockito
                .verify(highlightRepository, Mockito.never())
                .getHighlight(command.dto().sectionId(), command.dto().highlightId());

        Mockito
                .verify(folderShareRepository, Mockito.never())
                .getShareInvitation(command.dto().folderId(), command.dto().userId());
    }

    @Test
    @DisplayName("댓글 메타 정보 조회 - 실패 (하이라이트 없음)")
    void getInfoForCommentFailHighlightNotFound() {
        GetInfoForCommentCommand command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(GetInfoForCommentCommand.class)
                .sample();

        SectionEntity section = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(SectionEntity.class)
                .set("sectionId", command.dto().sectionId())
                .set("record.recordId", command.dto().recordId())
                .set("record.folder.folderId", command.dto().folderId())
                .sample();

        Mockito
                .when(sectionRepository.getSectionJoinFolderAndRecord(command.dto().sectionId(), command.dto().folderId(), command.dto().recordId()))
                .thenReturn(Optional.of(section));

        Mockito
                .when(highlightRepository.getHighlight(command.dto().sectionId(), command.dto().highlightId()))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> getInfoForCommentUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_HIGHLIGHT);

        Mockito
                .verify(sectionRepository, Mockito.times(1))
                .getSectionJoinFolderAndRecord(command.dto().sectionId(), command.dto().folderId(), command.dto().recordId());

        Mockito
                .verify(highlightRepository, Mockito.times(1))
                .getHighlight(command.dto().sectionId(), command.dto().highlightId());

        Mockito
                .verify(folderShareRepository, Mockito.never())
                .getShareInvitation(command.dto().folderId(), command.dto().userId());
    }

    @Test
    @DisplayName("댓글 메타 정보 조회 - 실패 (폴더 권한 없음)")
    void getInfoForCommentFailFolderShareNotFound() {
        GetInfoForCommentCommand command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(GetInfoForCommentCommand.class)
                .sample();

        SectionEntity section = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(SectionEntity.class)
                .set("sectionId", command.dto().sectionId())
                .set("record.recordId", command.dto().recordId())
                .set("record.folder.folderId", command.dto().folderId())
                .sample();

        HighlightEntity highlight = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(HighlightEntity.class)
                .set("highlightId", command.dto().highlightId())
                .set("section.sectionId", command.dto().sectionId())
                .sample();

        Mockito
                .when(sectionRepository.getSectionJoinFolderAndRecord(command.dto().sectionId(), command.dto().folderId(), command.dto().recordId()))
                .thenReturn(Optional.of(section));

        Mockito
                .when(highlightRepository.getHighlight(command.dto().sectionId(), command.dto().highlightId()))
                .thenReturn(Optional.of(highlight));

        Mockito
                .when(folderShareRepository.getShareInvitation(command.dto().folderId(), command.dto().userId()))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> getInfoForCommentUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_SHARE);

        Mockito
                .verify(sectionRepository, Mockito.times(1))
                .getSectionJoinFolderAndRecord(command.dto().sectionId(), command.dto().folderId(), command.dto().recordId());

        Mockito
                .verify(highlightRepository, Mockito.times(1))
                .getHighlight(command.dto().sectionId(), command.dto().highlightId());

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .getShareInvitation(command.dto().folderId(), command.dto().userId());
    }
}
