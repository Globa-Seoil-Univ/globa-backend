package org.y2k2.globa.application.folder.usecase;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.y2k2.globa.application.folder.command.UpdateFolderNameCommand;
import org.y2k2.globa.application.folder.usecase.UpdateFolderNameUseCase;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;

@ExtendWith(SpringExtension.class)
public class UpdateFolderNameUseCaseTest {
    private UpdateFolderNameUseCase updateFolderNameUseCase;

    @MockBean
    private FolderRepository folderRepository;

    @BeforeEach
    void setUp() {
        updateFolderNameUseCase = new UpdateFolderNameUseCase(folderRepository);
    }

    @Test
    @DisplayName("폴더 이름 수정 - 성공")
    public void updateFolderNameTest() {
        UpdateFolderNameCommand command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(UpdateFolderNameCommand.class);

        Mockito.when(folderRepository.save(ArgumentMatchers.any(FolderEntity.class)))
                .thenReturn(ArgumentMatchers.any(FolderEntity.class));

        updateFolderNameUseCase.execute(command);

        Mockito.verify(folderRepository, Mockito.times(1))
                .save(ArgumentMatchers.any(FolderEntity.class));
    }
}
