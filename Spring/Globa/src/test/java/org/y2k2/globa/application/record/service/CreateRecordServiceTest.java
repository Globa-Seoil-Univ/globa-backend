package org.y2k2.globa.application.record.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.application.kafka.dto.request.RequestKafkaDto;
import org.y2k2.globa.application.record.command.CreateRecordCommand;
import org.y2k2.globa.application.record.dto.request.RequestPostRecordDto;
import org.y2k2.globa.application.record.usecase.CreateRecordUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.crypto.AESUtil;
import org.y2k2.globa.common.util.kafka.KafkaProducer;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CreateRecordServiceTest {
    @InjectMocks
    private CreateRecordService createRecordService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;
    @Mock
    private CreateRecordUseCase createRecordUseCase;
    @Mock
    private FolderRepository folderRepository;
    @Mock
    private AESUtil aesUtil;
    @Mock
    private KafkaProducer kafkaProducer;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(createRecordService, "topic", "test-topic");
        ReflectionTestUtils.setField(createRecordService, "topicKey", "test-key");
    }

    @Test
    @DisplayName("문서 생성 - 성공")
    void create() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(UserEntity.class);

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(FolderEntity.class);

        RequestPostRecordDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestPostRecordDto.class);

        Mockito.when(findUserUseCase.execute(Mockito.anyLong()))
                .thenReturn(user);

        Mockito.when(folderRepository.getFolder(Mockito.anyLong()))
                .thenReturn(java.util.Optional.of(folder));

        Mockito.doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.when(createRecordUseCase.execute(Mockito.any(CreateRecordCommand.class)))
                .thenReturn(1L);

        Mockito.when(aesUtil.encrypt(Mockito.anyLong()))
                .thenReturn("encryptedUserId");

        Mockito.doNothing()
                .when(kafkaProducer)
                .send(Mockito.anyString(), Mockito.anyString(), Mockito.any(RequestKafkaDto.class));

        createRecordService.create(1L, dto, 1L);

        Mockito.verify(kafkaProducer, Mockito.times(1))
                .send(Mockito.anyString(), Mockito.anyString(), Mockito.any(RequestKafkaDto.class));
    }

    @Test
    @DisplayName("문서 생성 - 폴더 없음")
    void createFolderNotFound() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(UserEntity.class);

        RequestPostRecordDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestPostRecordDto.class);

        Mockito.when(findUserUseCase.execute(Mockito.anyLong()))
                .thenReturn(user);

        Mockito.when(folderRepository.getFolder(Mockito.anyLong()))
                .thenReturn(java.util.Optional.empty());

        Assertions.assertThatThrownBy(() -> createRecordService.create(1L, dto, 1L))
                .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_FOLDER);

        Mockito.verify(verifyFolderAccessibleUseCase, Mockito.times(0))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(createRecordUseCase, Mockito.times(0))
                .execute(Mockito.any(CreateRecordCommand.class));

        Mockito.verify(aesUtil, Mockito.times(0))
                .encrypt(Mockito.anyLong());

        Mockito.verify(kafkaProducer, Mockito.times(0))
                .send(Mockito.anyString(), Mockito.anyString(), Mockito.any(RequestKafkaDto.class));
    }
}
