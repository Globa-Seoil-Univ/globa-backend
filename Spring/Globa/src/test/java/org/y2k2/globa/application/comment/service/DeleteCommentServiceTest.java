package org.y2k2.globa.application.comment.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.y2k2.globa.application.comment.command.GetMyCommentCommand;
import org.y2k2.globa.application.comment.dto.request.RequestCommentWithIdsDto;
import org.y2k2.globa.application.comment.usecase.GetMyCommentUseCase;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderWritableUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.domain.highlight.repository.HighlightRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class DeleteCommentServiceTest {
    @InjectMocks
    private DeleteCommentService deleteCommentService;

    @Mock
    private VerifyFolderWritableUseCase verifyFolderWritableUseCase;
    @Mock
    private GetMyCommentUseCase getMyCommentUseCase;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private HighlightRepository highlightRepository;
    @Mock
    private CacheManager cacheManager;

    @Test
    @DisplayName("댓글 삭제 - 성공 (마지막 댓글인 경우)")
    void deleteComment_Success_Last() {
        Long commentId = 1L;
        RequestCommentWithIdsDto idsDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestCommentWithIdsDto.class);

        CommentEntity comment = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CommentEntity.class)
                .set("commentId", commentId)
                .set("isDeleted", false)
                .sample();

        HighlightEntity highlight = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(HighlightEntity.class)
                .set("highlightId", idsDto.highlightId())
                .sample();

        List<CommentEntity> deletedComments = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CommentEntity.class)
                .set("highlight", highlight)
                .set("isDeleted", true)
                .set("deletedTime", new CustomTimestamp().getTimestamp())
                .sampleList(10);

        Mockito
                .doNothing()
                .when(verifyFolderWritableUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(getMyCommentUseCase.execute(Mockito.any(GetMyCommentCommand.class)))
                .thenReturn(comment);

        // 마지막 댓글인지 확인하는 메서드가 true를 반환하도록 설정
        Mockito
                .when(commentRepository.isLastAliveComment(commentId))
                .thenReturn(true);

        Mockito
                .when(commentRepository.getAllDeletedComment(commentId))
                .thenReturn(deletedComments);

        Mockito
                .when(highlightRepository.getHighlight(idsDto.sectionId(), highlight.getHighlightId()))
                .thenReturn(Optional.of(highlight));

        Mockito
                .doNothing()
                .when(commentRepository)
                .deleteAll(Mockito.anyList());

        Mockito
                .doNothing()
                .when(highlightRepository)
                .delete(Mockito.any(HighlightEntity.class));

        Cache cache = Mockito.mock(Cache.class);

        Mockito
                .when(cacheManager.getCache("aggregateRecord"))
                .thenReturn(cache);
        Mockito
                .doNothing()
                .when(cache)
                .evict(idsDto.recordId());

        deleteCommentService.delete(idsDto, commentId);

        Mockito
                .verify(verifyFolderWritableUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(getMyCommentUseCase, Mockito.times(1))
                .execute(Mockito.any(GetMyCommentCommand.class));

        Mockito
                .verify(commentRepository, Mockito.times(1))
                .isLastAliveComment(commentId);

        Mockito
                .verify(commentRepository, Mockito.times(1))
                .getAllDeletedComment(commentId);

        Mockito
                .verify(highlightRepository, Mockito.times(1))
                .getHighlight(idsDto.sectionId(), highlight.getHighlightId());

        Mockito
                .verify(commentRepository, Mockito.times(1))
                .deleteAll(Mockito.anyList());

        Mockito
                .verify(highlightRepository, Mockito.times(1))
                .delete(Mockito.any(HighlightEntity.class));

        Mockito
                .verify(cacheManager, Mockito.times(1))
                .getCache("aggregateRecord");

        Mockito
                .verify(cache, Mockito.times(1))
                .evict(idsDto.recordId());
    }

    @Test
    @DisplayName("댓글 삭제 - 성공 (마지막 댓글이 아닌 경우)")
    void deleteComment_Success_Not_Last() {
        Long commentId = 1L;
        RequestCommentWithIdsDto idsDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestCommentWithIdsDto.class);

        CommentEntity comment = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CommentEntity.class)
                .set("commentId", commentId)
                .set("isDeleted", false)
                .sample();

        Mockito
                .doNothing()
                .when(verifyFolderWritableUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(getMyCommentUseCase.execute(Mockito.any(GetMyCommentCommand.class)))
                .thenReturn(comment);

        // 마지막 댓글인지 확인하는 메서드가 false를 반환하도록 설정
        Mockito
                .when(commentRepository.isLastAliveComment(commentId))
                .thenReturn(false);

        Mockito
                .when(commentRepository.save(Mockito.any(CommentEntity.class)))
                .thenReturn(comment);

        deleteCommentService.delete(idsDto, commentId);

        Mockito
                .verify(verifyFolderWritableUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(getMyCommentUseCase, Mockito.times(1))
                .execute(Mockito.any(GetMyCommentCommand.class));

        Mockito
                .verify(commentRepository, Mockito.times(1))
                .isLastAliveComment(commentId);

        Mockito
                .verify(commentRepository, Mockito.times(1))
                .save(Mockito.any(CommentEntity.class));
    }

    @Test
    @DisplayName("댓글 삭제 - 성공 (isLastAliveComment가 true이지만 삭제된 댓글이 없는 경우)")
    void deleteComment_Success_No_Deleted_Comments() {
        Long commentId = 1L;
        RequestCommentWithIdsDto idsDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestCommentWithIdsDto.class);

        CommentEntity comment = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CommentEntity.class)
                .set("commentId", commentId)
                .set("isDeleted", false)
                .sample();

        Mockito
                .doNothing()
                .when(verifyFolderWritableUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(getMyCommentUseCase.execute(Mockito.any(GetMyCommentCommand.class)))
                .thenReturn(comment);

        // 마지막 댓글인지 확인하는 메서드가 true를 반환하도록 설정
        Mockito
                .when(commentRepository.isLastAliveComment(commentId))
                .thenReturn(true);

        Mockito
                .when(commentRepository.getAllDeletedComment(commentId))
                .thenReturn(List.of());

        Assertions
                .assertThatThrownBy(() -> deleteCommentService.delete(idsDto, commentId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_COMMENT);

        Mockito
                .verify(verifyFolderWritableUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(getMyCommentUseCase, Mockito.times(1))
                .execute(Mockito.any(GetMyCommentCommand.class));

        Mockito
                .verify(commentRepository, Mockito.times(1))
                .isLastAliveComment(commentId);

        Mockito
                .verify(commentRepository, Mockito.times(1))
                .getAllDeletedComment(commentId);
    }
}
