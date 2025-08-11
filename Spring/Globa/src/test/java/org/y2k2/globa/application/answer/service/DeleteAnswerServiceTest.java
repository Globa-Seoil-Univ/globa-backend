package org.y2k2.globa.application.answer.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
public class DeleteAnswerServiceTest {
    @InjectMocks
    private DeleteAnswerService deleteAnswerService;

    @Mock
    private VerifyUserWritableUseCase verifyUserWritableUseCase;

    @Mock
    private InquiryRepository inquiryRepository;
    @Mock
    private AnswerRepository answerRepository;

    @Test
    @DisplayName("답변 삭제 - 성공")
    void deleteAnswer_Success() {
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
                .when(inquiryRepository.save(Mockito.any(InquiryEntity.class)))
                .thenReturn(inquiry);

        Mockito
                .doNothing()
                .when(answerRepository)
                .delete(Mockito.any(AnswerEntity.class));

        deleteAnswerService.delete(inquiry.getInquiryId(), answer.getAnswerId(), user.getUserId());

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
                .verify(inquiryRepository, Mockito.times(1))
                .save(Mockito.any(InquiryEntity.class));

        Mockito
                .verify(answerRepository, Mockito.times(1))
                .delete(Mockito.any(AnswerEntity.class));
    }

    @Test
    @DisplayName("답변 삭제 - 실패 (문의 X)")
    void deleteAnswer_Fail_InquiryNotFound() {
        Long inquiryId = 1L,
                answerId = 1L;

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("isDeleted", false)
                .sample();

        Mockito
                .doNothing()
                .when(verifyUserWritableUseCase)
                .execute(user.getUserId());

        Mockito
                .when(inquiryRepository.getInquiry(1L))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> deleteAnswerService.delete(inquiryId, answerId, user.getUserId()))
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
                .getAnswerById(Mockito.anyLong());

        Mockito
                .verify(inquiryRepository, Mockito.never())
                .save(Mockito.any(InquiryEntity.class));

        Mockito
                .verify(answerRepository, Mockito.never())
                .delete(Mockito.any(AnswerEntity.class));
    }

    @Test
    @DisplayName("답변 삭제 - 실패 (답변 X)")
    void deleteAnswer_Fail_AnswerNotFound() {
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

        Mockito
                .doNothing()
                .when(verifyUserWritableUseCase)
                .execute(user.getUserId());

        Mockito
                .when(inquiryRepository.getInquiry(inquiry.getInquiryId()))
                .thenReturn(Optional.of(inquiry));

        Mockito
                .when(answerRepository.getAnswerById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> deleteAnswerService.delete(inquiry.getInquiryId(), 1L, user.getUserId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_ANSWER);

        Mockito
                .verify(verifyUserWritableUseCase, Mockito.times(1))
                .execute(user.getUserId());

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .getInquiry(inquiry.getInquiryId());

        Mockito
                .verify(answerRepository, Mockito.times(1))
                .getAnswerById(Mockito.anyLong());

        Mockito
                .verify(inquiryRepository, Mockito.never())
                .save(Mockito.any(InquiryEntity.class));

        Mockito
                .verify(answerRepository, Mockito.never())
                .delete(Mockito.any(AnswerEntity.class));
    }
}
