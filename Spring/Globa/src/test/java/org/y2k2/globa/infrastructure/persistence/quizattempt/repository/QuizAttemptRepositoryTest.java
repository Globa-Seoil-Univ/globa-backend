package org.y2k2.globa.infrastructure.persistence.quizattempt.repository;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import net.jqwik.api.Arbitraries;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.y2k2.globa.domain.quizattemp.repository.QuizAttemptRepository;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.quiz.QuizFixture;
import org.y2k2.globa.fixture.record.RecordFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.config.RepositoryIntegrationTest;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;
import org.y2k2.globa.infrastructure.persistence.quizattemp.projection.QuizGradeProjection;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RepositoryIntegrationTest
public class QuizAttemptRepositoryTest {
    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private FolderFixture folderFixture;
    @Autowired
    private RecordFixture recordFixture;
    @Autowired
    private QuizFixture quizFixture;

    private UserEntity user;
    private RecordEntity record;

    @BeforeEach
    void setUp() {
        user = userFixture.save(
                UserFixture.builder().build()
        );

        FolderEntity folder = folderFixture.save(
                FolderFixture.builder()
                        .user(user)
                        .build()
        );

        record = recordFixture.save(
                RecordFixture.builder()
                        .folder(folder)
                        .user(user)
                        .build()
        );
    }

    @Test
    @DisplayName("퀴즈 기록 생성 - 성공")
    void createQuizAttempt_Success() {
        QuizEntity quiz = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(QuizEntity.class)
                .set("record", record)
                .set("question", Arbitraries.strings().alpha().ofMinLength(1))
                .sample();

        QuizAttemptEntity quizAttemptEntity = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(QuizAttemptEntity.class)
                .set("user", user)
                .set("quiz", quizFixture.save(
                        QuizFixture.builder()
                                .record(record)
                                .question(quiz.getQuestion())
                                .answer(quiz.getAnswer())
                                .build()
                ))
                .sample();

        QuizAttemptEntity savedEntity = quizAttemptRepository.save(quizAttemptEntity);

        Assertions
                .assertThat(savedEntity)
                .isNotNull()
                .extracting(QuizAttemptEntity::getAttemptId)
                .isNotNull();
    }

    @Test
    @DisplayName("다수 퀴즈 기록 생성 - 성공")
    void createMultipleQuizAttempts_Success() {
        List<QuizEntity> quizzes = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(QuizEntity.class)
                .set("record", record)
                .set("question", Arbitraries.strings().alpha().ofMinLength(1))
                .sampleList(5);

        List<QuizAttemptEntity> quizAttemptEntity = quizzes.stream()
                .map(quiz -> FixtureMonkey.builder()
                        .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build()
                        .giveMeBuilder(QuizAttemptEntity.class)
                        .set("user", user)
                        .set("quiz", quizFixture.save(
                                QuizFixture.builder()
                                        .record(record)
                                        .question(quiz.getQuestion())
                                        .answer(quiz.getAnswer())
                                        .build()
                        ))
                        .sample())
                .toList();

        quizAttemptRepository.saveAll(quizAttemptEntity);
    }

    @Test
    @DisplayName("모든 퀴즈 기록 조회 - 성공 (최근 7일 기준)")
    void getQuizAttemptByUserInDays_Success() {
        QuizEntity quiz = quizFixture.save(
                QuizFixture.builder()
                        .record(record)
                        .build()
        );

        QuizAttemptEntity quizAttemptEntity = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(QuizAttemptEntity.class)
                .set("user", user)
                .set("quiz", quiz)
                .sample();

        quizAttemptRepository.save(quizAttemptEntity);
        List<QuizGradeProjection> quizAttempts = quizAttemptRepository.getQuizAttemptByUserInDays(user.getUserId());

        Assertions
                .assertThat(quizAttempts)
                .isNotEmpty()
                .allSatisfy(attempt -> {
                    log.info("Quiz Attempt = {}", attempt.getQuizGrade());

                    Assertions
                            .assertThat(attempt.getQuizGrade()).isNotNull();
                });
    }

    @Test
    @DisplayName("모든 퀴즈 기록 조회 - 성공 (8일 이상, 빈 목록)")
    void getQuizAttemptByUserInDays_Empty_Success() {
        QuizEntity quiz = quizFixture.save(
                QuizFixture.builder()
                        .record(record)
                        .build()
        );

        QuizAttemptEntity quizAttemptEntity = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(QuizAttemptEntity.class)
                .set("user", user)
                .set("quiz", quiz)
                .sample();

        QuizAttemptEntity savedAttempt = quizAttemptRepository.save(quizAttemptEntity);

        savedAttempt.setCreatedTime(
                savedAttempt.getCreatedTime().minusDays(8)
        );

        quizAttemptRepository.save(savedAttempt);

        List<QuizGradeProjection> quizAttempts = quizAttemptRepository.getQuizAttemptByUserInDays(1L);

        Assertions
                .assertThat(quizAttempts)
                .isEmpty();
    }

    @Test
    @DisplayName("모든 퀴즈 기록 조회 - 성공 (존재하지 않는 사용자 ID, 빈 목록)")
    void getQuizAttemptByUserInDays_NonExistentUser_Success() {
        QuizEntity quiz = quizFixture.save(
                QuizFixture.builder()
                        .record(record)
                        .build()
        );

        QuizAttemptEntity quizAttemptEntity = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(QuizAttemptEntity.class)
                .set("user", user)
                .set("quiz", quiz)
                .sample();

        quizAttemptRepository.save(quizAttemptEntity);

        List<QuizGradeProjection> quizAttempts = quizAttemptRepository.getQuizAttemptByUserInDays(999L);

        Assertions
                .assertThat(quizAttempts)
                .isEmpty();
    }

    @Test
    @DisplayName("퀴즈 기록 목록 조회 - 성공 (특정 레코드 ID로 조회)")
    void getQuizAttemptByUserAndRecordId_Success() {
        QuizEntity quiz = quizFixture.save(
                QuizFixture.builder()
                        .record(record)
                        .build()
        );

        QuizAttemptEntity quizAttemptEntity = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(QuizAttemptEntity.class)
                .set("user", user)
                .set("quiz", quiz)
                .sample();

        quizAttemptRepository.save(quizAttemptEntity);

        List<QuizGradeProjection> quizAttempts = quizAttemptRepository.getQuizAttemptByUserAndRecordId(user.getUserId(), record.getRecordId());

        Assertions
                .assertThat(quizAttempts)
                .isNotEmpty()
                .allSatisfy(attempt -> {
                    log.info("Quiz Attempt = {}", attempt.getQuizGrade());

                    Assertions
                            .assertThat(attempt.getQuizGrade()).isNotNull();
                });
    }

    @Test
    @DisplayName("퀴즈 기록 목록 조회 - 성공 (특정 레코드 ID로 조회, 빈 목록)")
    void getQuizAttemptByUserAndRecordId_Empty_Success() {
        QuizEntity quiz = quizFixture.save(
                QuizFixture.builder()
                        .record(record)
                        .build()
        );

        QuizAttemptEntity quizAttemptEntity = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(QuizAttemptEntity.class)
                .set("user", user)
                .set("quiz", quiz)
                .sample();

        quizAttemptRepository.save(quizAttemptEntity);

        List<QuizGradeProjection> quizAttempts = quizAttemptRepository.getQuizAttemptByUserAndRecordId(user.getUserId(), 999L);

        Assertions
                .assertThat(quizAttempts)
                .isEmpty();
    }

    @Test
    @DisplayName("퀴즈 기록 목록 조회 - 성공 (존재하지 않는 사용자 ID, 빈 목록)")
    void getQuizAttemptByUserAndRecordId_NonExistentUser_Success() {
        QuizEntity quiz = quizFixture.save(
                QuizFixture.builder()
                        .record(record)
                        .build()
        );

        QuizAttemptEntity quizAttemptEntity = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(QuizAttemptEntity.class)
                .set("user", user)
                .set("quiz", quiz)
                .sample();

        quizAttemptRepository.save(quizAttemptEntity);

        List<QuizGradeProjection> quizAttempts = quizAttemptRepository.getQuizAttemptByUserAndRecordId(999L, record.getRecordId());

        Assertions
                .assertThat(quizAttempts)
                .isEmpty();
    }

    @Test
    @DisplayName("모든 퀴즈 기록 조회 - 성공 (33.3% 정답률)")
    void getQuizAttemptByUserInDays_33PercentSuccessRate() {
        Boolean[] isCorrects = {true, false, false};

        QuizEntity quiz = quizFixture.save(
                QuizFixture.builder()
                        .record(record)
                        .build()
        );

        List<QuizAttemptEntity> attempts = Arrays.stream(isCorrects)
                .map(isCorrect -> FixtureMonkey.builder()
                        .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build()
                        .giveMeBuilder(QuizAttemptEntity.class)
                        .set("user", user)
                        .set("isCorrect", isCorrect)
                        .set("quiz", quizFixture.save(
                                QuizFixture.builder()
                                        .record(record)
                                        .question(quiz.getQuestion())
                                        .answer(quiz.getAnswer())
                                        .build()
                        ))
                        .sample())
                .toList();

        quizAttemptRepository.saveAll(attempts);

        List<QuizGradeProjection> quizAttempts = quizAttemptRepository.getQuizAttemptByUserInDays(user.getUserId());

        Assertions
                .assertThat(quizAttempts)
                .isNotEmpty()
                .allSatisfy(attempt -> {
                    log.info("Quiz Attempt = {}", attempt.getQuizGrade());

                    Assertions
                            .assertThat(attempt.getQuizGrade()).isGreaterThanOrEqualTo(33);
                });
    }

    @Test
    @DisplayName("퀴즈 기록 목록 조회 - 성공 (33.3% 정답률, 특정 레코드 ID로 조회)")
    void getQuizAttemptByUserAndRecordId_33PercentSuccessRate() {
        Boolean[] isCorrects = {true, false, false};

        QuizEntity quiz = quizFixture.save(
                QuizFixture.builder()
                        .record(record)
                        .build()
        );

        List<QuizAttemptEntity> attempts = Arrays.stream(isCorrects)
                .map(isCorrect -> FixtureMonkey.builder()
                        .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build()
                        .giveMeBuilder(QuizAttemptEntity.class)
                        .set("user", user)
                        .set("isCorrect", isCorrect)
                        .set("quiz", quizFixture.save(
                                QuizFixture.builder()
                                        .record(record)
                                        .question(quiz.getQuestion())
                                        .answer(quiz.getAnswer())
                                        .build()
                        ))
                        .sample())
                .toList();

        quizAttemptRepository.saveAll(attempts);

        List<QuizGradeProjection> quizAttempts = quizAttemptRepository.getQuizAttemptByUserAndRecordId(user.getUserId(), record.getRecordId());

        Assertions
                .assertThat(quizAttempts)
                .isNotEmpty()
                .allSatisfy(attempt -> {
                    log.info("Quiz Attempt = {}", attempt.getQuizGrade());

                    Assertions
                            .assertThat(attempt.getQuizGrade()).isGreaterThanOrEqualTo(33);
                });
    }
}
