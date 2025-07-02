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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.application.comment.dto.request.RequestCommentWithIdsDto;
import org.y2k2.globa.application.comment.dto.response.ResponseReplyDto;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.domain.highlight.repository.HighlightRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class GetRepliesServiceTest {
    @InjectMocks
    private GetRepliesService getRepliesService;

    @Mock
    private VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;
    @Mock
    private HighlightRepository highlightRepository;
    @Mock
    private CommentRepository commentRepository;

    @Test
    @DisplayName("대댓글 목록 조회 - 성공")
    void getReplies_Success() {
        RequestCommentWithIdsDto idsDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestCommentWithIdsDto.class);

        CommentEntity parentComment = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CommentEntity.class)
                .set("commentId", idsDto.parentId())
                .set("highlight.highlightId", idsDto.highlightId())
                .set("parent", null)
                .sample();

        Pageable pageable = PageRequest.of(0, 10);
        List<CommentEntity> replies = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CommentEntity.class)
                .set("highlight.highlightId", idsDto.highlightId())
                .set("parent", parentComment)
                .sampleList(5);
        Page<CommentEntity> page = new PageImpl<>(replies, pageable, replies.size());

        Mockito
                .doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(highlightRepository.isHighlightInSection(idsDto.sectionId(), idsDto.highlightId()))
                .thenReturn(true);

        Mockito
                .when(commentRepository.isExistParentComment(idsDto.highlightId(), idsDto.parentId()))
                .thenReturn(true);

        Mockito
                .when(commentRepository.getChildComments(idsDto.parentId(), pageable))
                .thenReturn(page);

        ResponseReplyDto response = getRepliesService.get(idsDto, 1, 10);

        Assertions
                .assertThat(response)
                .isNotNull();

        Assertions
                .assertThat(response.total())
                .isEqualTo(replies.size());

        Assertions
                .assertThat(response.comments())
                .allSatisfy(reply -> {
                    Assertions
                            .assertThat(reply.getCommentId())
                            .isIn(replies.stream()
                                    .map(CommentEntity::getCommentId)
                                    .toList());
                });

        Mockito
                .verify(verifyFolderAccessibleUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(highlightRepository, Mockito.times(1))
                .isHighlightInSection(idsDto.sectionId(), idsDto.highlightId());

        Mockito
                .verify(commentRepository, Mockito.times(1))
                .isExistParentComment(idsDto.highlightId(), idsDto.parentId());

        Mockito
                .verify(commentRepository, Mockito.times(1))
                .getChildComments(idsDto.parentId(), pageable);
    }

    @Test
    @DisplayName("대댓글 목록 조회 - 실패 (하이라이트가 섹션에 없음)")
    void getReplies_Fail_HighlightNotInSection() {
        RequestCommentWithIdsDto idsDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestCommentWithIdsDto.class);

        Pageable pageable = PageRequest.of(0, 10);

        Mockito
                .doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(highlightRepository.isHighlightInSection(idsDto.sectionId(), idsDto.highlightId()))
                .thenReturn(false);

        Assertions
                .assertThatThrownBy(() -> getRepliesService.get(idsDto, 1, 10))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_HIGHLIGHT);

        Mockito
                .verify(verifyFolderAccessibleUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(highlightRepository, Mockito.times(1))
                .isHighlightInSection(idsDto.sectionId(), idsDto.highlightId());

        Mockito
                .verify(commentRepository, Mockito.times(0))
                .isExistParentComment(idsDto.highlightId(), idsDto.parentId());

        Mockito
                .verify(commentRepository, Mockito.times(0))
                .getChildComments(idsDto.parentId(), pageable);
    }

    @Test
    @DisplayName("대댓글 목록 조회 - 실패 (부모 댓글 X)")
    void getReplies_Fail_ParentCommentNotExist() {
        RequestCommentWithIdsDto idsDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestCommentWithIdsDto.class);

        Mockito
                .doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(highlightRepository.isHighlightInSection(idsDto.sectionId(), idsDto.highlightId()))
                .thenReturn(true);

        Mockito
                .when(commentRepository.isExistParentComment(idsDto.highlightId(), idsDto.parentId()))
                .thenReturn(false);

        Assertions
                .assertThatThrownBy(() -> getRepliesService.get(idsDto, 1, 10))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_PARENT_COMMENT);

        Mockito
                .verify(verifyFolderAccessibleUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(highlightRepository, Mockito.times(1))
                .isHighlightInSection(idsDto.sectionId(), idsDto.highlightId());

        Mockito
                .verify(commentRepository, Mockito.times(1))
                .isExistParentComment(idsDto.highlightId(), idsDto.parentId());

        Mockito
                .verify(commentRepository, Mockito.times(0))
                .getChildComments(idsDto.parentId(), PageRequest.of(0, 10));
    }
}
