package org.y2k2.globa.fixture.user;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.*;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

@Component
@Getter
public class AnalysisFixture {
    @Autowired
    private FolderFactory folderFactory;
    @Autowired
    private RecordFactory recordFactory;
    @Autowired
    private QuizFactory quizFactory;
    @Autowired
    private QuizAttemptFactory quizAttemptFactory;
    @Autowired
    private StudyFactory studyFactory;
    @Autowired
    private KeywordFactory keywordFactory;
    @Autowired
    private FolderRoleFactory folderRoleFactory;
    @Autowired
    private FolderShareFactory folderShareFactory;

    public void createFixture(UserEntity user, LocalDateTime createdTime) {
        FolderEntity folder = createFolder(user);
        FolderRoleEntity folderRole = createFolderRole(user, folder);
        createFolderShare(user, folder, folderRole);
        RecordEntity record = createRecord(user, folder);
        QuizEntity quiz = createQuiz(record);
        QuizAttemptEntity quizAttempt = createQuizAttempt(user, quiz);
        updateQuizAttempt(quizAttempt, createdTime);
        createStudy(user, record);
        createKeyword(record);
    }

    private FolderEntity createFolder(UserEntity user) {
        folderFactory.setUser(user);
        return folderFactory.createAndSave();
    }

    private FolderRoleEntity createFolderRole(UserEntity user, FolderEntity folder) {
        return folderRoleFactory.createAndSave();
    }

    private void createFolderShare(UserEntity user, FolderEntity folder, FolderRoleEntity folderRole) {
        folderShareFactory.setOwner(user);
        folderShareFactory.setTarget(user);
        folderShareFactory.setRole(folderRole);
        folderShareFactory.setFolder(folder);
        folderShareFactory.createAndSave();
    }

    private RecordEntity createRecord(UserEntity user, FolderEntity folder) {
        recordFactory.setUser(user);
        recordFactory.setFolder(folder);
        return recordFactory.createAndSave();
    }

    private QuizEntity createQuiz(RecordEntity record) {
        quizFactory.setRecord(record);
        return quizFactory.createAndSave();
    }

    private QuizAttemptEntity createQuizAttempt(UserEntity user, QuizEntity quiz) {
        quizAttemptFactory.setQuiz(quiz);
        quizAttemptFactory.setUser(user);
        quizAttemptFactory.setIsCorrect(true);
        return quizAttemptFactory.createAndSave();
    }

    private QuizAttemptEntity updateQuizAttempt(QuizAttemptEntity entity, LocalDateTime createdTime) {
        quizAttemptFactory.setCreatedTime(createdTime);
        return quizAttemptFactory.updateAndSave(entity);
    }

    private void createStudy(UserEntity user, RecordEntity record) {
        studyFactory.setUser(user);
        studyFactory.setRecord(record);
        studyFactory.createAndSave();
    }

    private void createKeyword(RecordEntity record) {
        keywordFactory.setRecord(record);
        keywordFactory.createAndSave();
    }
}
