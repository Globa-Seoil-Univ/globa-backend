package org.y2k2.globa.application.user.command;

import lombok.NonNull;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Objects;

public record UpdateUserCommand(
        UserEntity user,
        String name,
        FileDto profileImage,
        Boolean uploadNofi,
        Boolean shareNofi,
        Boolean eventNofi
) {
    public UpdateUserCommand {
        Objects.requireNonNull(user, "user must not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UserEntity user;
        private String name;
        private FileDto profileImage;
        private Boolean uploadNofi;
        private Boolean shareNofi;
        private Boolean eventNofi;

        public Builder user(@NonNull UserEntity user) {
            this.user = user;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder profileImage(FileDto profileImage) {
            this.profileImage = profileImage;
            return this;
        }

        public Builder uploadNofi(Boolean uploadNofi) {
            this.uploadNofi = uploadNofi;
            return this;
        }

        public Builder shareNofi(Boolean shareNofi) {
            this.shareNofi = shareNofi;
            return this;
        }

        public Builder eventNofi(Boolean eventNofi) {
            this.eventNofi = eventNofi;
            return this;
        }

        public UpdateUserCommand build() {
            return new UpdateUserCommand(user, name, profileImage, uploadNofi, shareNofi, eventNofi);
        }
    }
}
