package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.FolderShareEntity;
import org.y2k2.globa.entity.UserEntity;

@Getter
@Setter
@AllArgsConstructor
public class RequestNotificationWithFolderShareAddUserDto {
    private UserEntity toUser;
    private FolderEntity folder;
    private FolderShareEntity folderShare;
}
