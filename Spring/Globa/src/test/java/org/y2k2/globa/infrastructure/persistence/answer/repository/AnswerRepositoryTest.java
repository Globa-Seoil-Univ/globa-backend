package org.y2k2.globa.infrastructure.persistence.answer.repository;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.y2k2.globa.domain.answer.repository.AnswerRepository;
import org.y2k2.globa.fixture.inquiry.InquiryFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.answer.entity.AnswerEntity;
import org.y2k2.globa.infrastructure.persistence.config.RepositoryIntegrationTest;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@Slf4j
@RepositoryIntegrationTest
public class AnswerRepositoryTest {
    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private InquiryFixture inquiryFixture;

    private UserEntity user;
    private InquiryEntity inquiry;

    @BeforeEach
    void setup() {
        user = userFixture.save(
                UserFixture.builder()
                        .build()
        );

        inquiry = inquiryFixture.save(
                InquiryFixture.builder()
                        .user(user)
                        .isSolved(false)
                        .build()
        );
    }

    @Test
    @DisplayName("답변 생성 - 성공")
    void createAnswer_Success() {
        AnswerEntity answer = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(AnswerEntity.class)
                .set("user", user)
                .set("inquiry", inquiry)
                .sample();

        AnswerEntity savedAnswer = answerRepository.save(answer);

        log.info("Saved answer = {}", savedAnswer.getAnswerId());

        Assertions.assertThat(savedAnswer.getAnswerId()).isNotNull();
        Assertions.assertThat(savedAnswer.getUser()).isEqualTo(user);
        Assertions.assertThat(savedAnswer.getInquiry()).isEqualTo(inquiry);
        Assertions.assertThat(savedAnswer.getTitle()).isEqualTo(answer.getTitle());
        Assertions.assertThat(savedAnswer.getContent()).isEqualTo(answer.getContent());
    }

    @Test
    @DisplayName("답변 삭제 - 성공")
    void deleteAnswer_Success() {
        AnswerEntity answer = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(AnswerEntity.class)
                .set("user", user)
                .set("inquiry", inquiry)
                .sample();

        AnswerEntity savedAnswer = answerRepository.save(answer);
        log.info("Saved answer = {}", savedAnswer.getAnswerId());

        answerRepository.delete(savedAnswer);
        Assertions.assertThat(answerRepository.getAnswerById(savedAnswer.getAnswerId())).isEmpty();
    }

    @Test
    @DisplayName("답변 조회 - 성공 (답변 ID로 조회)")
    void getAnswerById_Success() {
        AnswerEntity answer = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(AnswerEntity.class)
                .set("user", user)
                .set("inquiry", inquiry)
                .sample();

        AnswerEntity savedAnswer = answerRepository.save(answer);
        log.info("Saved answer = {}", savedAnswer.getAnswerId());

        Optional<AnswerEntity> retrievedAnswer = answerRepository.getAnswerById(savedAnswer.getAnswerId());
        Assertions.assertThat(retrievedAnswer).isPresent();

        Assertions.assertThat(retrievedAnswer.get().getAnswerId()).isEqualTo(savedAnswer.getAnswerId());
        Assertions.assertThat(retrievedAnswer.get().getUser()).isEqualTo(user);
        Assertions.assertThat(retrievedAnswer.get().getInquiry()).isEqualTo(inquiry);
        Assertions.assertThat(retrievedAnswer.get().getTitle()).isEqualTo(savedAnswer.getTitle());
        Assertions.assertThat(retrievedAnswer.get().getContent()).isEqualTo(savedAnswer.getContent());
    }

    @Test
    @DisplayName("답변 조회 - 성공 (문의 ID로 조회)")
    void getAnswerByInquiryId_Success() {
        AnswerEntity answer = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(AnswerEntity.class)
                .set("user", user)
                .set("inquiry", inquiry)
                .sample();

        AnswerEntity savedAnswer = answerRepository.save(answer);
        log.info("Saved answer = {}", savedAnswer.getAnswerId());

        Optional<AnswerEntity> retrievedAnswer = answerRepository.getAnswerByInquiryId(inquiry.getInquiryId());
        Assertions.assertThat(retrievedAnswer).isPresent();

        Assertions.assertThat(retrievedAnswer.get().getAnswerId()).isEqualTo(savedAnswer.getAnswerId());
        Assertions.assertThat(retrievedAnswer.get().getUser()).isEqualTo(user);
        Assertions.assertThat(retrievedAnswer.get().getInquiry()).isEqualTo(inquiry);
        Assertions.assertThat(retrievedAnswer.get().getTitle()).isEqualTo(savedAnswer.getTitle());
        Assertions.assertThat(retrievedAnswer.get().getContent()).isEqualTo(savedAnswer.getContent());
    }
}
