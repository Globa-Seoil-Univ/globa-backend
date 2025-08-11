package org.y2k2.globa.application.comment.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.comment.command.GetMyCommentCommand;
import org.y2k2.globa.application.comment.dto.request.RequestCommentDto;
import org.y2k2.globa.application.comment.dto.request.RequestCommentWithIdsDto;
import org.y2k2.globa.application.comment.usecase.GetMyCommentUseCase;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderWritableUseCase;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;

@ExtendWith(MockitoExtension.class)
public class UpdateCommentServiceTest {
    @InjectMocks
    private UpdateCommentService updateCommentService;

    @Mock
    private VerifyFolderWritableUseCase verifyFolderWritableUseCase;
    @Mock
    private GetMyCommentUseCase getMyCommentUseCase;
    @Mock
    private CommentRepository commentRepository;

    @Test
    @DisplayName("댓글 수정 - 성공")
    void update_Success() {
        Long commentId = 1L;
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

        CommentEntity comment = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CommentEntity.class)
                .set("commentId", commentId)
                .set("highlight.highlightId", idsDto.highlightId())
                .sample();

        Mockito
                .doNothing()
                .when(verifyFolderWritableUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(getMyCommentUseCase.execute(Mockito.any(GetMyCommentCommand.class)))
                .thenReturn(comment);

        Mockito
                .when(commentRepository.save(Mockito.any(CommentEntity.class)))
                .thenReturn(comment);

        updateCommentService.update(idsDto, commentId, request);

        Mockito.verify(verifyFolderWritableUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(getMyCommentUseCase, Mockito.times(1))
                .execute(Mockito.any(GetMyCommentCommand.class));

        Mockito.verify(commentRepository, Mockito.times(1))
                .save(Mockito.any(CommentEntity.class));
    }
}
