package org.y2k2.globa.application.notification.usecase;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import net.jqwik.api.Arbitraries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.y2k2.globa.application.notification.command.SendCommentNotificationCommand;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithFolderShareCommentDto;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class SendCommentNotificationUseCaseTest {
    @InjectMocks
    private SendCommentNotificationUseCase sendCommentNotificationUseCase;

    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private FolderShareRepository folderShareRepository;

    @Test
    @DisplayName("댓글 알림 전송 - 성공")
    void sendCommentNotificationSuccess() {
        UserEntity sender = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .sample();

        List<UserEntity> receiver = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", Arbitraries.longs().greaterOrEqual(1))
                .sampleList(5);

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .set("user", sender)
                .sample();

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("recordId", 1L)
                .set("folder", folder)
                .set("user", sender)
                .sample();

        SendCommentNotificationCommand command = SendCommentNotificationCommand.of(
                sender,
                folder,
                record
        );

        List<FolderShareEntity> targets = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("folder", command.folder())
                .set("invitationStatus", InvitationStatus.ACCEPT)
                .sampleList(3);

        Mockito
                .when(folderShareRepository.getAllShareInvitations(command.folder().getFolderId()))
                .thenReturn(targets);

        Mockito
                .doNothing()
                .when(publisher)
                .publishEvent(Mockito.anyList());

        sendCommentNotificationUseCase.execute(command);

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .getAllShareInvitations(command.folder().getFolderId());

        Mockito.verify(publisher, Mockito.times(1))
                .publishEvent(Mockito.anyList());
    }
}
