package org.y2k2.globa.application.answer.service;

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
import org.y2k2.globa.application.answer.dto.request.RequestAnswerDto;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.application.userrole.usecase.VerifyUserWritableUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.answer.repository.AnswerRepository;
import org.y2k2.globa.domain.inquiry.repository.InquiryRepository;
import org.y2k2.globa.infrastructure.persistence.answer.entity.AnswerEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UpdateAnswerServiceTest {
    @InjectMocks
    private UpdateAnswerService updateAnswerService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private VerifyUserWritableUseCase verifyUserWritableUseCase;
    @Mock
    private InquiryRepository inquiryRepository;
    @Mock
    private AnswerRepository answerRepository;

    @Test
    @DisplayName("답변 수정 - 성공")
    void updateAnswer_Success() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("isDeleted", false)
                .sample();
        InquiryEntity inquiry = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(InquiryEntity.class)
                .set("user", user)
                .set("isSolved", true)
                .sample();
        AnswerEntity answer = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(AnswerEntity.class)
                .set("user", user)
                .set("inquiry", inquiry)
                .sample();
        RequestAnswerDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestAnswerDto.class);

        Mockito
                .doNothing()
                .when(verifyUserWritableUseCase)
                .execute(user.getUserId());

        Mockito
                .when(inquiryRepository.getInquiry(inquiry.getInquiryId()))
                .thenReturn(Optional.of(inquiry));

        Mockito
                .when(answerRepository.getAnswerById(answer.getAnswerId()))
                .thenReturn(Optional.of(answer));

        Mockito
                .when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);

        Mockito
                .when(inquiryRepository.save(Mockito.any(InquiryEntity.class)))
                .thenReturn(inquiry);

        Mockito
                .when(answerRepository.save(Mockito.any(AnswerEntity.class)))
                .thenReturn(answer);

        updateAnswerService.update(inquiry.getInquiryId(), answer.getAnswerId(), dto, user.getUserId());

        Mockito
                .verify(verifyUserWritableUseCase, Mockito.times(1))
                .execute(user.getUserId());

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .getInquiry(inquiry.getInquiryId());

        Mockito
                .verify(answerRepository, Mockito.times(1))
                .getAnswerById(answer.getAnswerId());

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(user.getUserId());

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .save(Mockito.any(InquiryEntity.class));

        Mockito
                .verify(answerRepository, Mockito.times(1))
                .save(Mockito.any(AnswerEntity.class));
    }

    @Test
    @DisplayName("답변 수정 - 실패 (문의 X)")
    void updateAnswer_Failure_InquiryNotFound() {
        Long inquiryId = 1L,
                answerId = 1L;
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("isDeleted", false)
                .sample();
        RequestAnswerDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestAnswerDto.class);

        Mockito
                .doNothing()
                .when(verifyUserWritableUseCase)
                .execute(user.getUserId());

        Mockito
                .when(inquiryRepository.getInquiry(inquiryId))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> updateAnswerService.update(inquiryId, answerId, dto, user.getUserId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_INQUIRY);

        Mockito
                .verify(verifyUserWritableUseCase, Mockito.times(1))
                .execute(user.getUserId());

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .getInquiry(inquiryId);

        Mockito
                .verify(answerRepository, Mockito.never())
                .getAnswerById(answerId);

        Mockito
                .verify(findUserUseCase, Mockito.never())
                .execute(user.getUserId());

        Mockito
                .verify(inquiryRepository, Mockito.never())
                .save(Mockito.any(InquiryEntity.class));

        Mockito
                .verify(answerRepository, Mockito.never())
                .save(Mockito.any(AnswerEntity.class));
    }

    @Test
    @DisplayName("답변 수정 - 실패 (답변 X)")
    void updateAnswer_Failure_AnswerNotFound() {
        Long inquiryId = 1L,
                answerId = 1L;
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("isDeleted", false)
                .sample();
        InquiryEntity inquiry = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(InquiryEntity.class)
                .set("user", user)
                .set("isSolved", true)
                .sample();
        RequestAnswerDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestAnswerDto.class);

        Mockito
                .doNothing()
                .when(verifyUserWritableUseCase)
                .execute(user.getUserId());

        Mockito
                .when(inquiryRepository.getInquiry(inquiryId))
                .thenReturn(Optional.of(inquiry));

        Mockito
                .when(answerRepository.getAnswerById(answerId))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> updateAnswerService.update(inquiryId, answerId, dto, user.getUserId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_ANSWER);

        Mockito
                .verify(verifyUserWritableUseCase, Mockito.times(1))
                .execute(user.getUserId());

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .getInquiry(inquiryId);

        Mockito
                .verify(answerRepository, Mockito.times(1))
                .getAnswerById(answerId);

        Mockito
                .verify(findUserUseCase, Mockito.never())
                .execute(user.getUserId());

        Mockito
                .verify(inquiryRepository, Mockito.never())
                .save(Mockito.any(InquiryEntity.class));

        Mockito
                .verify(answerRepository, Mockito.never())
                .save(Mockito.any(AnswerEntity.class));
    }
}
