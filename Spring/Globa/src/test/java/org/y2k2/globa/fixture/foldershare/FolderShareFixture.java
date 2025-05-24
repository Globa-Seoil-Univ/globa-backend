package org.y2k2.globa.fixture.foldershare;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.foldershare.FolderShareFactory;
import org.y2k2.globa.fixture.AbstractFixture;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Component
public class FolderShareFixture extends AbstractFixture<FolderShareEntity> {
    @Autowired
    private FolderShareFactory folderShareFactory;

    @Override
    protected FolderShareEntity build() {
        return folderShareFactory.createAndSave();
    }

    public FolderShareFixture withInvitationStatus(InvitationStatus status) {
        folderShareFactory.setInvitationStatus(status);
        return this;
    }

    public FolderShareFixture withFolder(FolderEntity folder) {
        folderShareFactory.setFolder(folder);
        return this;
    }

    public FolderShareFixture withOwner(UserEntity user) {
        folderShareFactory.setOwner(user);
        return this;
    }

    public FolderShareFixture withTarget(UserEntity user) {
        folderShareFactory.setTarget(user);
        return this;
    }

    public FolderShareFixture withRole(FolderRoleEntity role) {
        folderShareFactory.setRole(role);
        return this;
    }
}
