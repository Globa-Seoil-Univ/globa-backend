package org.y2k2.globa.infrastructure.persistence.comment.entity;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class CommentEntityTest {
    CommentEntity comment;

    @BeforeEach
    void setUp() {
        comment = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(CommentEntity.class);
    }

    @Test
    @DisplayName("댓글 내용 수정 - 성공")
    void updateContent() {
        String newContent = "Updated comment content";
        comment.updateContent(newContent);

        Assertions.assertThat(comment.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("댓글 내용 수정 - 성공 (trim)")
    void updateContentTrim() {
        String newContent = "   Updated comment content   ";
        comment.updateContent(newContent);

        Assertions.assertThat(comment.getContent()).isEqualTo("Updated comment content");
    }

    @Test
    @DisplayName("댓글 내용 수정 - 실패 (null)")
    void updateContentFail() {
        String originalContent = comment.getContent();
        comment.updateContent(null);

        Assertions.assertThat(comment.getContent()).isEqualTo(originalContent);
    }
}
