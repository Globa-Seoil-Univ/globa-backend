package org.y2k2.globa.application.comment.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.comment.command.GetInfoForCommentCommand;
import org.y2k2.globa.application.comment.dto.common.InfoForCommentDto;
import org.y2k2.globa.application.comment.dto.request.RequestCommentDto;
import org.y2k2.globa.application.comment.dto.request.RequestCommentWithIdsDto;
import org.y2k2.globa.application.comment.usecase.GetInfoForCommentUseCase;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderWritableUseCase;
import org.y2k2.globa.application.notification.command.CreateNotificationCommand;
import org.y2k2.globa.application.notification.command.SendCommentNotificationCommand;
import org.y2k2.globa.application.notification.usecase.CreateNotificationUseCase;
import org.y2k2.globa.application.notification.usecase.SendCommentNotificationUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
public class CreateParentCommentServiceTest {
    @InjectMocks
    private CreateParentCommentService createParentCommentService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private GetInfoForCommentUseCase getInfoForCommentUseCase;
    @Mock
    private VerifyFolderWritableUseCase verifyFolderWritableUseCase;
    @Mock
    private CreateNotificationUseCase createNotificationUseCase;
    @Mock
    private SendCommentNotificationUseCase sendCommentNotificationUseCase;
    @Mock
    private CommentRepository commentRepository;

    @Test
    @DisplayName("부모 댓글 생성 - 성공")
    void createParentComment_Success() {
        RequestCommentWithIdsDto idsDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestCommentWithIdsDto.class);

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestCommentDto.class);

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", idsDto.userId())
                .sample();

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

        InfoForCommentDto info = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(InfoForCommentDto.class)
                .set("section", section)
                .set("highlight", highlight)
                .set("folderShare", folderShare)
                .sample();

        CommentEntity comment = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CommentEntity.class)
                .set("user", user)
                .set("highlight", highlight)
                .set("content", request.content())
                .sample();

        Mockito
                .doNothing()
                .when(verifyFolderWritableUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(findUserUseCase.execute(idsDto.userId()))
                .thenReturn(user);

        Mockito
                .when(getInfoForCommentUseCase.execute(Mockito.any(GetInfoForCommentCommand.class)))
                .thenReturn(info);

        Mockito
                .when(commentRepository.save(Mockito.any(CommentEntity.class)))
                .thenReturn(comment);

        Mockito
                .doNothing()
                .when(createNotificationUseCase)
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .doNothing()
                .when(sendCommentNotificationUseCase)
                .execute(Mockito.any(SendCommentNotificationCommand.class));

        createParentCommentService.create(idsDto, request);

        Mockito.verify(verifyFolderWritableUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(findUserUseCase, Mockito.times(1))
                .execute(idsDto.userId());

        Mockito.verify(getInfoForCommentUseCase, Mockito.times(1))
                .execute(Mockito.any(GetInfoForCommentCommand.class));

        Mockito.verify(commentRepository, Mockito.times(1))
                .save(Mockito.any(CommentEntity.class));

        Mockito.verify(createNotificationUseCase, Mockito.times(1))
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito.verify(sendCommentNotificationUseCase, Mockito.times(1))
                .execute(Mockito.any(SendCommentNotificationCommand.class));
    }
}