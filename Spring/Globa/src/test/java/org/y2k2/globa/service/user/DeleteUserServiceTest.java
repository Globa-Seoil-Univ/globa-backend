package org.y2k2.globa.service.user;

import com.navercorp.fixturemonkey.FixtureMonkey;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.survey.dto.request.RequestSurveyDto;
import org.y2k2.globa.application.user.service.DeleteUserService;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.survey.repository.SurveyRepository;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.survey.entity.SurveyEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class DeleteUserServiceTest {
    @InjectMocks
    private DeleteUserService deleteUserService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SurveyRepository surveyRepository;

    @Test
    @DisplayName("유저 삭제 - 성공")
    void deleteUser() {
        RequestSurveyDto dto = new RequestSurveyDto("BAC", "사용 불편함.");

        UserEntity user = FixtureMonkey.builder()
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .set("isDeleted", false)
                .sample();

        Mockito.when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);

        deleteUserService.delete(dto, user.getUserId());

        Mockito.verify(userRepository, Mockito.times(1)).save(user);
        Mockito.verify(surveyRepository, Mockito.times(1)).save(ArgumentMatchers.any(SurveyEntity.class));
    }

    @Test
    @DisplayName("유저 삭제 - 실패 (삭제된 유저)")
    void deleteUserDeleted() {
        RequestSurveyDto dto = new RequestSurveyDto("BAC", "사용 불편함.");

        UserEntity user = FixtureMonkey.builder()
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .set("isDeleted", true)
                .sample();

        Mockito.when(findUserUseCase.execute(user.getUserId()))
                .thenThrow(new CustomException(ErrorCode.DELETED_USER));

        Assertions.assertThatThrownBy(() -> deleteUserService.delete(dto, user.getUserId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DELETED_USER);

        Mockito.verify(userRepository, Mockito.times(0)).save(user);
        Mockito.verify(surveyRepository, Mockito.times(0)).save(ArgumentMatchers.any(SurveyEntity.class));
    }

    @Test
    @DisplayName("유저 삭제 - 실패 (유저 없음)")
    void deleteUserNotFound() {
        RequestSurveyDto dto = new RequestSurveyDto("BAC", "사용 불편함.");

        Mockito.when(findUserUseCase.execute(1L))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_USER));

        Assertions.assertThatThrownBy(() -> deleteUserService.delete(dto, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_USER);

        Mockito.verify(userRepository, Mockito.times(0)).save(ArgumentMatchers.any(UserEntity.class));
        Mockito.verify(surveyRepository, Mockito.times(0)).save(ArgumentMatchers.any(SurveyEntity.class));
    }
}
