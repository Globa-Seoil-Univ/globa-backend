package org.y2k2.globa.fixture.user;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.foldershare.FolderShareFixture;
import org.y2k2.globa.fixture.keyword.KeywordFixture;
import org.y2k2.globa.fixture.quiz.QuizFixture;
import org.y2k2.globa.fixture.quizattempt.QuizAttemptFixture;
import org.y2k2.globa.fixture.record.RecordFixture;
import org.y2k2.globa.fixture.study.StudyFixture;
import org.y2k2.globa.fixture.user.data.AnalysisData;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.keyword.entity.KeywordEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

@Component
@Getter
public class AnalysisFixtureBuilder {
    private final UserFixture userFixture;
    private final FolderFixture folderFixture;
    private final RecordFixture recordFixture;
    private final QuizFixture quizFixture;
    private final QuizAttemptFixture quizAttemptFixture;
    private final StudyFixture studyFixture;
    private final KeywordFixture keywordFixture;
    private final FolderRoleFixture folderRoleFixture;
    private final FolderShareFixture folderShareFixture;

    private UserEntity user;
    private FolderEntity folder;
    private FolderRoleEntity folderRole;
    private RecordEntity record;
    private QuizEntity quiz;
    private QuizAttemptEntity quizAttempt;
    private StudyEntity study;
    private KeywordEntity keyword;
    private LocalDateTime createdTime;

    @Autowired
    public AnalysisFixtureBuilder(UserFixture userFixture, FolderFixture folderFixture, RecordFixture recordFixture, QuizFixture quizFixture, QuizAttemptFixture quizAttemptFixture, StudyFixture studyFixture, KeywordFixture keywordFixture, FolderRoleFixture folderRoleFixture, FolderShareFixture folderShareFixture) {
        this.userFixture = userFixture;
        this.folderFixture = folderFixture;
        this.recordFixture = recordFixture;
        this.quizFixture = quizFixture;
        this.quizAttemptFixture = quizAttemptFixture;
        this.studyFixture = studyFixture;
        this.keywordFixture = keywordFixture;
        this.folderRoleFixture = folderRoleFixture;
        this.folderShareFixture = folderShareFixture;
    }

    public AnalysisFixtureBuilder withUser(UserEntity user) {
        this.user = user;
        return this;
    }

    public AnalysisFixtureBuilder withCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    public AnalysisData build() {
        if (user == null) {
            user = userFixture.save(
                    UserFixture
                            .builder()
                            .name("testUser")
                            .build()
            );
        }

        // 폴더 생성
        folder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(user)
                        .title("Test Folder")
                        .build()
        );

        // 폴더 역할 생성
        folderRole = folderRoleFixture.save(
                FolderRoleFixture
                        .builder()
                        .role(FolderRole.OWNER)
                        .build()
        );

        // 폴더 공유 생성
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(user)
                        .target(user)
                        .role(folderRole)
                        .folder(folder)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );

        // 레코드 생성
        record = recordFixture.save(
                RecordFixture
                        .builder()
                        .title("Test Record")
                        .user(user)
                        .folder(folder)
                        .path("/test/path")
                        .isShare(false)
                        .build()
        );

        // 퀴즈 생성
        quiz = quizFixture.save(
                QuizFixture
                        .builder()
                        .record(record)
                        .build()
        );

        // 퀴즈 시도 생성
        quizAttempt = quizAttemptFixture.save(
                QuizAttemptFixture
                        .builder()
                        .user(user)
                        .quiz(quiz)
                        .isCorrect(true)
                        .createdTime(new CustomTimestamp().getTimestamp())
                        .build()
        );

        // 학습 생성
        study = studyFixture.save(
                StudyFixture
                        .builder()
                        .user(user)
                        .record(record)
                        .studyTime(10L)
                        .createdTime(new CustomTimestamp().getTimestamp())
                        .build()
        );

        if (createdTime != null) {
            quizAttempt.setCreatedTime(createdTime);
            quizAttempt = quizAttemptFixture.save(quizAttempt);

            study.setCreatedTime(createdTime);
            study = studyFixture.save(study);
        }

        // 키워드 생성
        keyword = keywordFixture.save(
                KeywordFixture
                        .builder()
                        .record(record)
                        .build()
        );

        // 생성된 모든 엔티티를 포함하는 데이터 객체 반환
        return new AnalysisData(folder, folderRole, record, quiz, quizAttempt, study, keyword);
    }
}
