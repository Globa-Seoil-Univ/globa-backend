package org.y2k2.globa.infrastructure.persistence.quiz.repository;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.y2k2.globa.domain.quiz.repository.QuizRepository;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.quiz.QuizFixture;
import org.y2k2.globa.fixture.record.RecordFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.config.RepositoryIntegrationTest;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

@Slf4j
@RepositoryIntegrationTest
public class QuizRepositoryTest {
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private QuizTestRepositoryImpl saveRepository;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private FolderFixture folderFixture;
    @Autowired
    private RecordFixture recordFixture;

    private RecordEntity record;

    @BeforeEach
    void setUp() {
        UserEntity user = userFixture.save(
                UserFixture
                        .builder()
                        .build()
        );

        FolderEntity folder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(user)
                        .build()
        );

        record = recordFixture.save(
                RecordFixture
                        .builder()
                        .folder(folder)
                        .user(user)
                        .build()
        );
    }

    @Test
    @DisplayName("문서 내 모든 퀴즈 목록 조회 - 성공")
    void getAllQuizzes_Success() {
        List<QuizEntity> quizzes = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(QuizEntity.class)
                .set("record", record)
                .sampleList(10);

        List<QuizEntity> savedQuizzes = saveRepository.saveAll(quizzes);
        List<QuizEntity> foundQuizzes = quizRepository.getAllQuizzes(record.getRecordId());

        Assertions
                .assertThat(foundQuizzes)
                .hasSize(savedQuizzes.size())
                .allSatisfy(quiz -> {
                    log.info("Quiz id = {}, question = {}, answer = {}", quiz.getQuizId(), quiz.getQuestion(), quiz.getAnswer());

                    Assertions.assertThat(quiz.getRecord()).isEqualTo(record);
                    Assertions.assertThat(quiz.getQuizId()).isNotNull();
                });
    }

    @Test
    @DisplayName("문서 내 특정 퀴즈 목록 조회 - 성공")
    void getAllByQuizzesInRecord_Success() {
        List<QuizEntity> quizzes = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(QuizEntity.class)
                .set("record", record)
                .sampleList(10);

        List<QuizEntity> savedQuizzes = saveRepository.saveAll(quizzes);
        List<Long> quizIds = savedQuizzes.stream().map(QuizEntity::getQuizId).toList();

        // 5개만 조회
        List<QuizEntity> foundQuizzes = quizRepository.getAllByQuizzesInRecord(record, quizIds.subList(0 ,5));

        Assertions
                .assertThat(foundQuizzes)
                .hasSize(5)
                .allSatisfy(quiz -> {
                    log.info("Quiz id = {}, question = {}, answer = {}", quiz.getQuizId(), quiz.getQuestion(), quiz.getAnswer());

                    Assertions.assertThat(quiz.getRecord()).isEqualTo(record);
                    Assertions.assertThat(quiz.getQuizId()).isNotNull();
                });
    }

    @Test
    @DisplayName("퀴즈 삭제 - 성공")
    void deleteAllQuizzes_Success() {
        List<QuizEntity> quizzes = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(QuizEntity.class)
                .set("record", record)
                .sampleList(10);

        List<QuizEntity> savedQuizzes = saveRepository.saveAll(quizzes);
        quizRepository.deleteAll(savedQuizzes);

        List<QuizEntity> foundQuizzes = quizRepository.getAllQuizzes(record.getRecordId());
        Assertions.assertThat(foundQuizzes).isEmpty();
    }
}
