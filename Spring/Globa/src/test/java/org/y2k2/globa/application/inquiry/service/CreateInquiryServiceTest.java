package org.y2k2.globa.application.inquiry.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import net.jqwik.api.Arbitraries;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.inquiry.dto.request.RequestInquiryDto;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.domain.inquiry.repository.InquiryRepository;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
public class CreateInquiryServiceTest {
    @InjectMocks
    private CreateInquiryService createInquiryService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private InquiryRepository inquiryRepository;

    @Test
    @DisplayName("문의 생성 - 성공")
    void createInquiry() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(UserEntity.class);

        RequestInquiryDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestInquiryDto.class);

        InquiryEntity inquiry = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(InquiryEntity.class)
                .set("inquiryId", 1L) // AUTO_INCREMENT ID는 테스트에서 직접 설정
                .set("title", dto.title())
                .set("content", dto.content())
                .set("user", user)
                .set("isSolved", false)
                .sample();

        Mockito
                .when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);

        Mockito
                .when(inquiryRepository.save(Mockito.any(InquiryEntity.class)))
                .thenReturn(inquiry);

        Long inquiryId = createInquiryService.create(dto, user.getUserId());

        Assertions
                .assertThat(inquiryId)
                .as("AUTO_INCREMENT ID가 생성되어야 합니다.")
                .isEqualTo(1);

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(user.getUserId());

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .save(Mockito.any(InquiryEntity.class));
    }
}
