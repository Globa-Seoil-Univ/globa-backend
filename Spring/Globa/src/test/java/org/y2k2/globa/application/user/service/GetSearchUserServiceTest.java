package org.y2k2.globa.application.user.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.user.dto.response.ResponseUserSearchDto;
import org.y2k2.globa.application.user.service.GetSearchUserService;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class GetSearchUserServiceTest {
    @InjectMocks
    private GetSearchUserService getSearchUserService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("유저 검색 - 성공")
    void getSearchUser() {
        String code = "testCode";

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("code", code)
                .set("isDeleted", false)
                .sample();

        Mockito.when(userRepository.getUserByCode(code))
                .thenReturn(Optional.of(user));

        ResponseUserSearchDto response = getSearchUserService.getSearchUser(code);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.code()).isEqualTo(code);
    }

    @Test
    @DisplayName("유저 검색 - 실패 (삭제된 유저)")
    void getSearchUserFailDeleted() {
        String code = "testCode";

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("code", code)
                .set("isDeleted", true)
                .sample();

        Mockito.when(userRepository.getUserByCode(code))
                .thenReturn(Optional.of(user));

        ResponseUserSearchDto response = getSearchUserService.getSearchUser(code);

        Assertions.assertThat(response).isNull();
    }

    @Test
    @DisplayName("유저 검색 - 실패 (유저 없음)")
    void getSearchUserFailNotFound() {
        String code = "testCode";

        Mockito.when(userRepository.getUserByCode(code))
                .thenReturn(Optional.empty());

        ResponseUserSearchDto response = getSearchUserService.getSearchUser(code);

        Assertions.assertThat(response).isNull();
    }
}
