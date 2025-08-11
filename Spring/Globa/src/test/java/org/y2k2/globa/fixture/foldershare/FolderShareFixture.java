package org.y2k2.globa.fixture.foldershare;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.repository.FolderShareRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Import(FolderShareRepositoryImpl.class)
@Component
public class FolderShareFixture implements Fixture<FolderShareEntity> {
    @Autowired
    private FolderShareRepository folderShareRepository;

    @Override
    public FolderShareEntity save(FolderShareEntity entity) {
        return folderShareRepository.save(entity);
    }

    public static FolderShareBuilder builder() {
        return new FolderShareBuilder();
    }

    public static class FolderShareBuilder {
        private InvitationStatus status = InvitationStatus.ACCEPT;
        private FolderEntity folder;
        private UserEntity owner;
        private UserEntity target;
        private FolderRoleEntity role;

        private FolderShareBuilder() {}

        public FolderShareBuilder status(InvitationStatus status) {
            this.status = status;
            return this;
        }

        public FolderShareBuilder folder(FolderEntity folder) {
            this.folder = folder;
            return this;
        }

        public FolderShareBuilder owner(UserEntity owner) {
            this.owner = owner;
            return this;
        }

        public FolderShareBuilder target(UserEntity target) {
            this.target = target;
            return this;
        }

        public FolderShareBuilder role(FolderRoleEntity role) {
            this.role = role;
            return this;
        }

        public FolderShareEntity build() {
            FolderShareEntity folderShare = new FolderShareEntity();
            folderShare.setInvitationStatus(status);
            folderShare.setFolder(folder);
            folderShare.setOwnerUser(owner);
            folderShare.setTargetUser(target);
            folderShare.setRole(role);
            return folderShare;
        }
    }
}
