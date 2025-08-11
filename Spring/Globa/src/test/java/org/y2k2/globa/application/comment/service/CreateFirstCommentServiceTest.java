package org.y2k2.globa.application.comment.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.comment.dto.request.RequestCommentWithIdsDto;
import org.y2k2.globa.application.comment.dto.request.RequestFirstCommentDto;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderWritableUseCase;
import org.y2k2.globa.application.notification.command.CreateNotificationCommand;
import org.y2k2.globa.application.notification.command.SendCommentNotificationCommand;
import org.y2k2.globa.application.notification.usecase.CreateNotificationUseCase;
import org.y2k2.globa.application.notification.usecase.SendCommentNotificationUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.domain.highlight.repository.HighlightRepository;
import org.y2k2.globa.domain.section.repository.SectionRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CreateFirstCommentServiceTest {
    @InjectMocks
    private CreateFirstCommentService createFirstCommentService;

    @Mock
    private VerifyFolderWritableUseCase verifyFolderWritableUseCase;
    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private CreateNotificationUseCase createNotificationUseCase;
    @Mock
    private SendCommentNotificationUseCase sendCommentNotificationUseCase;
    @Mock
    private FolderShareRepository folderShareRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private HighlightRepository highlightRepository;

    @Test
    @DisplayName("최초 댓글 생성 - 성공")
    void createFirstComment_Success() {
        RequestCommentWithIdsDto idsDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestCommentWithIdsDto.class);

        RequestFirstCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestFirstCommentDto.class);

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", idsDto.userId())
                .sample();

        SectionEntity section = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(SectionEntity.class)
                .set("sectionId", idsDto.sectionId())
                .set("record.recordId", idsDto.recordId())
                .set("record.folder.folderId", idsDto.folderId())
                .sample();

        HighlightEntity highlight = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(HighlightEntity.class)
                .set("highlightId", 1L)
                .set("section.sectionId", idsDto.sectionId())
                .set("startIndex", request.startIdx())
                .set("endIndex", request.endIdx())
                .sample();

        CommentEntity comment = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CommentEntity.class)
                .set("user.userId", idsDto.userId())
                .set("highlight.highlightId", idsDto.highlightId())
                .set("isDeleted", false)
                .set("deletedTime", null)
                .set("content", request.content())
                .sample();

        FolderShareEntity folderShare = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("folder.folderId", idsDto.folderId())
                .set("targetUser.userId", idsDto.userId())
                .set("invitationStatus", InvitationStatus.ACCEPT)
                .sample();

        Mockito
                .doNothing()
                .when(verifyFolderWritableUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(findUserUseCase.execute(idsDto.userId()))
                .thenReturn(user);

        Mockito
                .when(highlightRepository.hasHighlightInRange(
                        idsDto.sectionId(),
                        request.startIdx(),
                        request.endIdx()
                ))
                .thenReturn(false);

        Mockito
                .when(sectionRepository.getSectionJoinFolderAndRecord(
                        idsDto.sectionId(),
                        idsDto.folderId(),
                        idsDto.recordId()
                ))
                .thenReturn(Optional.of(section));

        Mockito
                .when(highlightRepository.save(Mockito.any(HighlightEntity.class)))
                .thenReturn(highlight);

        Mockito
                .when(commentRepository.save(Mockito.any(CommentEntity.class)))
                .thenReturn(comment);

        Mockito
                .when(folderShareRepository.getShareInvitation(
                        idsDto.folderId(),
                        idsDto.userId()
                ))
                .thenReturn(Optional.of(folderShare));

        Mockito
                .doNothing()
                .when(createNotificationUseCase)
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .doNothing()
                .when(sendCommentNotificationUseCase)
                .execute(Mockito.any(SendCommentNotificationCommand.class));

        Long highlightId = createFirstCommentService.create(idsDto, request);

        log.info("Created highlight ID = {}", highlightId);

        Assertions
                .assertThat(highlightId)
                .isNotNull()
                .isPositive()
                .isEqualTo(highlight.getHighlightId());

        Mockito
                .verify(verifyFolderWritableUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(idsDto.userId());

        Mockito
                .verify(highlightRepository, Mockito.times(1))
                .hasHighlightInRange(idsDto.sectionId(), request.startIdx(), request.endIdx());

        Mockito
                .verify(sectionRepository, Mockito.times(1))
                .getSectionJoinFolderAndRecord(idsDto.sectionId(), idsDto.folderId(), idsDto.recordId());

        Mockito
                .verify(highlightRepository, Mockito.times(1))
                .save(Mockito.any(HighlightEntity.class));

        Mockito
                .verify(commentRepository, Mockito.times(1))
                .save(Mockito.any(CommentEntity.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .getShareInvitation(idsDto.folderId(), idsDto.userId());

        Mockito
                .verify(createNotificationUseCase, Mockito.times(1))
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(sendCommentNotificationUseCase, Mockito.times(1))
                .execute(Mockito.any(SendCommentNotificationCommand.class));
    }

    @Test
    @DisplayName("최초 댓글 생성 - 실패 (편집 권한 X)")
    void createFirstComment_Fail_NoPermission() {
        RequestCommentWithIdsDto idsDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestCommentWithIdsDto.class);

        RequestFirstCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestFirstCommentDto.class);

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", idsDto.userId())
                .sample();

        Mockito
                .doNothing()
                .when(verifyFolderWritableUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(findUserUseCase.execute(idsDto.userId()))
                .thenReturn(user);

        Mockito
                .when(highlightRepository.hasHighlightInRange(
                        idsDto.sectionId(),
                        request.startIdx(),
                        request.endIdx()
                ))
                .thenReturn(true);

        Assertions
                .assertThatThrownBy(() -> createFirstCommentService.create(idsDto, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HIGHLIGHT_DUPLICATED);

        Mockito
                .verify(verifyFolderWritableUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(idsDto.userId());

        Mockito
                .verify(highlightRepository, Mockito.times(1))
                .hasHighlightInRange(idsDto.sectionId(), request.startIdx(), request.endIdx());

        Mockito
                .verify(sectionRepository, Mockito.never())
                .getSectionJoinFolderAndRecord(idsDto.sectionId(), idsDto.folderId(), idsDto.recordId());

        Mockito
                .verify(highlightRepository, Mockito.never())
                .save(Mockito.any(HighlightEntity.class));

        Mockito
                .verify(commentRepository, Mockito.never())
                .save(Mockito.any(CommentEntity.class));

        Mockito
                .verify(folderShareRepository, Mockito.never())
                .getShareInvitation(idsDto.folderId(), idsDto.userId());

        Mockito
                .verify(createNotificationUseCase, Mockito.never())
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(sendCommentNotificationUseCase, Mockito.never())
                .execute(Mockito.any(SendCommentNotificationCommand.class));
    }

    @Test
    @DisplayName("최초 댓글 생성 - 실패 (섹션 없음)")
    void createFirstComment_Fail_NoSection() {
        RequestCommentWithIdsDto idsDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestCommentWithIdsDto.class);

        RequestFirstCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestFirstCommentDto.class);

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", idsDto.userId())
                .sample();

        Mockito
                .doNothing()
                .when(verifyFolderWritableUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(findUserUseCase.execute(idsDto.userId()))
                .thenReturn(user);

        Mockito
                .when(highlightRepository.hasHighlightInRange(
                        idsDto.sectionId(),
                        request.startIdx(),
                        request.endIdx()
                ))
                .thenReturn(false);

        Mockito
                .when(sectionRepository.getSectionJoinFolderAndRecord(
                        idsDto.sectionId(),
                        idsDto.folderId(),
                        idsDto.recordId()
                ))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> createFirstCommentService.create(idsDto, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_SECTION);

        Mockito
                .verify(verifyFolderWritableUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(idsDto.userId());

        Mockito
                .verify(highlightRepository, Mockito.times(1))
                .hasHighlightInRange(idsDto.sectionId(), request.startIdx(), request.endIdx());

        Mockito
                .verify(sectionRepository, Mockito.times(1))
                .getSectionJoinFolderAndRecord(idsDto.sectionId(), idsDto.folderId(), idsDto.recordId());

        Mockito
                .verify(highlightRepository, Mockito.never())
                .save(Mockito.any(HighlightEntity.class));

        Mockito
                .verify(commentRepository, Mockito.never())
                .save(Mockito.any(CommentEntity.class));

        Mockito
                .verify(folderShareRepository, Mockito.never())
                .getShareInvitation(idsDto.folderId(), idsDto.userId());

        Mockito
                .verify(createNotificationUseCase, Mockito.never())
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(sendCommentNotificationUseCase, Mockito.never())
                .execute(Mockito.any(SendCommentNotificationCommand.class));
    }
}
