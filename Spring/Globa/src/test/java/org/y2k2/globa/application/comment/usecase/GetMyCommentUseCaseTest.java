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
import org.y2k2.globa.application.comment.command.GetMyCommentCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;

import java.util.Optional;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class GetMyCommentUseCaseTest {
    @InjectMocks
    private GetMyCommentUseCase getMyCommentUseCase;

    @Mock
    private CommentRepository commentRepository;

    @Test
    @DisplayName("내 댓글 조회 - 성공")
    void setGetMyComment() {
        GetMyCommentCommand command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(GetMyCommentCommand.class);

        CommentEntity comment = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CommentEntity.class)
                .set("user.userId", command.userId())
                .set("highlight.highlightId", command.highlightId())
                .set("commentId", command.commentId())
                .sample();

        Mockito
                .when(commentRepository.getComment(command.highlightId(), command.commentId()))
                .thenReturn(Optional.of(comment));

        CommentEntity result = getMyCommentUseCase.execute(command);

        Assertions
                .assertThat(result)
                .isNotNull()
                .isEqualTo(comment);

        Mockito.verify(commentRepository, Mockito.times(1))
                .getComment(command.highlightId(), command.commentId());
    }

    @Test
    @DisplayName("내 댓글 조회 - 실패 (댓글 없음)")
    void setGetMyCommentFailNotFoundComment() {
        GetMyCommentCommand command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(GetMyCommentCommand.class);

        Mockito
                .when(commentRepository.getComment(command.highlightId(), command.commentId()))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> getMyCommentUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_COMMENT);

        Mockito.verify(commentRepository, Mockito.times(1))
                .getComment(command.highlightId(), command.commentId());
    }

    @Test
    @DisplayName("내 댓글 조회 - 실패 (댓글 작성자가 아님)")
    void setGetMyCommentFailNotOwner() {
        GetMyCommentCommand command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(GetMyCommentCommand.class);

        CommentEntity comment = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CommentEntity.class)
                .set("user.userId", command.userId() + 1L)  // 작성자 ID를 다르게 설정
                .set("highlight.highlightId", command.highlightId())
                .set("commentId", command.commentId())
                .sample();

        Mockito
                .when(commentRepository.getComment(command.highlightId(), command.commentId()))
                .thenReturn(Optional.of(comment));

        Assertions
                .assertThatThrownBy(() -> getMyCommentUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISMATCH_COMMENT_OWNER);

        Mockito.verify(commentRepository, Mockito.times(1))
                .getComment(command.highlightId(), command.commentId());
    }
}
