package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.entity.*;

@Getter
@Setter
@AllArgsConstructor
public class RequestNotificationWithFolderShareCommentDto {
    private UserEntity fromUser;
    private FolderEntity folder;
    private FolderShareEntity folderShare;
    private RecordEntity record;
    private CommentEntity comment;
}
