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
import org.springframework.context.ApplicationEventPublisher;
import org.y2k2.globa.application.answer.dto.request.RequestAnswerDto;
import org.y2k2.globa.application.notification.command.CreateNotificationCommand;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithInquiryDto;
import org.y2k2.globa.application.notification.usecase.CreateNotificationUseCase;
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
public class CreateAnswerServiceTest {
    @InjectMocks
    private CreateAnswerService createAnswerService;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private VerifyUserWritableUseCase verifyUserWritableUseCase;
    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private CreateNotificationUseCase createNotificationUseCase;

    @Mock
    private InquiryRepository inquiryRepository;
    @Mock
    private AnswerRepository answerRepository;

    @Test
    @DisplayName("답변 생성 - 성공")
    void createAnswer_Success() {
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
                .sample();

        RequestAnswerDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestAnswerDto.class);

        AnswerEntity answer = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(AnswerEntity.class)
                .set("user", user)
                .set("inquiry", inquiry)
                .set("title", dto.title())
                .set("content", dto.content())
                .sample();

        Mockito
                .doNothing()
                .when(verifyUserWritableUseCase)
                .execute(user.getUserId());

        Mockito
                .when(inquiryRepository.getInquiry(inquiry.getInquiryId()))
                .thenReturn(Optional.of(inquiry));

        Mockito
                .when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);

        Mockito
                .when(inquiryRepository.save(Mockito.any(InquiryEntity.class)))
                .thenReturn(inquiry);

        Mockito
                .when(answerRepository.save(Mockito.any(AnswerEntity.class)))
                .thenReturn(answer);

        Mockito
                .doNothing()
                .when(publisher)
                .publishEvent(Mockito.any(RequestNotificationWithInquiryDto.class));

        createAnswerService.create(inquiry.getInquiryId(), dto, user.getUserId());

        Mockito
                .verify(verifyUserWritableUseCase, Mockito.times(1))
                .execute(user.getUserId());

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .getInquiry(inquiry.getInquiryId());

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(user.getUserId());

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .save(Mockito.any(InquiryEntity.class));

        Mockito
                .verify(answerRepository, Mockito.times(1))
                .save(Mockito.any(AnswerEntity.class));

        Mockito
                .verify(createNotificationUseCase, Mockito.times(1))
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(publisher, Mockito.times(1))
                .publishEvent(Mockito.any(RequestNotificationWithInquiryDto.class));
    }

    @Test
    @DisplayName("답변 생성 - 실패 (문의 X)")
    void createAnswer_Fail_InquiryNotFound() {
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
                .when(inquiryRepository.getInquiry(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> createAnswerService.create(1L, dto, user.getUserId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_INQUIRY);

        Mockito
                .verify(verifyUserWritableUseCase, Mockito.times(1))
                .execute(user.getUserId());

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .getInquiry(Mockito.anyLong());

        Mockito
                .verify(findUserUseCase, Mockito.never())
                .execute(user.getUserId());

        Mockito
                .verify(inquiryRepository, Mockito.never())
                .save(Mockito.any(InquiryEntity.class));

        Mockito
                .verify(answerRepository, Mockito.never())
                .save(Mockito.any(AnswerEntity.class));

        Mockito
                .verify(createNotificationUseCase, Mockito.never())
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(publisher, Mockito.never())
                .publishEvent(Mockito.any(RequestNotificationWithInquiryDto.class));
    }

    @Test
    @DisplayName("답변 생성 - 실패 (이미 답변된 문의)")
    void createAnswer_Fail_InquiryAlreadySolved() {
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
                .set("isSolved", true) // 이미 답변된 문의
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

        Assertions
                .assertThatThrownBy(() -> createAnswerService.create(inquiry.getInquiryId(), dto, user.getUserId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INQUIRY_ANSWER_DUPLICATED);

        Mockito
                .verify(verifyUserWritableUseCase, Mockito.times(1))
                .execute(user.getUserId());

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .getInquiry(inquiry.getInquiryId());

        Mockito
                .verify(findUserUseCase, Mockito.never())
                .execute(user.getUserId());

        Mockito
                .verify(inquiryRepository, Mockito.never())
                .save(Mockito.any(InquiryEntity.class));

        Mockito
                .verify(answerRepository, Mockito.never())
                .save(Mockito.any(AnswerEntity.class));

        Mockito
                .verify(createNotificationUseCase, Mockito.never())
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(publisher, Mockito.never())
                .publishEvent(Mockito.any(RequestNotificationWithInquiryDto.class));
    }
}
