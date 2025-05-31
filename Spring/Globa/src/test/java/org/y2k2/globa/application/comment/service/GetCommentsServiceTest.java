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
import org.y2k2.globa.application.comment.dto.response.ResponseCommentDto;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.domain.highlight.repository.HighlightRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class GetCommentsServiceTest {
    @InjectMocks
    private GetCommentsService getCommentsService;

    @Mock
    private VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;
    @Mock
    private HighlightRepository highlightRepository;
    @Mock
    private CommentRepository commentRepository;

    @Test
    @DisplayName("부모 댓글 목록 조회 - 성공")
    void getParentComments_Success() {
        RequestCommentWithIdsDto idsDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestCommentWithIdsDto.class);

        Pageable pageable = PageRequest.of(0, 10);

        List<CommentEntity> parents = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CommentEntity.class)
                .set("highlight.highlightId", idsDto.highlightId())
                .set("parent", null)
                .set("hasReply", false)
                .sampleList(5);

        Page<CommentEntity> page = new PageImpl<>(parents, pageable, parents.size());

        Mockito
                .doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(highlightRepository.isHighlightInSection(idsDto.sectionId(), idsDto.highlightId()))
                .thenReturn(true);

        Mockito
                .when(commentRepository.getParentComments(Mockito.anyLong(), Mockito.any(Pageable.class)))
                .thenReturn(page);

        ResponseCommentDto response = getCommentsService.get(idsDto, 1, 10);

        Assertions
                .assertThat(response)
                .isNotNull();

        Assertions
                .assertThat(response.getTotal())
                .isEqualTo(parents.size());

        Assertions
                .assertThat(response.getComments())
                .allSatisfy(comment -> {
                    Assertions
                            .assertThat(comment.getCommentId())
                            .isIn(parents.stream()
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
                .getParentComments(Mockito.anyLong(), Mockito.any(Pageable.class));
    }

    @Test
    @DisplayName("부모 댓글 목록 조회 - 실패 (하이라이트가 섹션에 없음)")
    void getParentComments_Fail_HighlightNotInSection() {
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
                .thenReturn(false);

        Assertions
                .assertThatThrownBy(() -> getCommentsService.get(idsDto, 1, 10))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_HIGHLIGHT);

        Mockito
                .verify(verifyFolderAccessibleUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(highlightRepository, Mockito.times(1))
                .isHighlightInSection(idsDto.sectionId(), idsDto.highlightId());

        Mockito
                .verify(commentRepository, Mockito.never())
                .getParentComments(Mockito.anyLong(), Mockito.any(Pageable.class));
    }
}
