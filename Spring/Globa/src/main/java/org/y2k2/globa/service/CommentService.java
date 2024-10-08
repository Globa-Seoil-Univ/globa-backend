package org.y2k2.globa.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.y2k2.globa.dto.*;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.mapper.CommentMapper;
import org.y2k2.globa.mapper.NotificationMapper;
import org.y2k2.globa.type.InvitationStatus;
import org.y2k2.globa.type.NotificationType;
import org.y2k2.globa.type.Role;
import org.y2k2.globa.util.CustomTimestamp;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final FirebaseMessaging firebaseMessaging;
    private final CommentRepository commentRepository;
    private final FolderShareRepository folderShareRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final HighlightRepository highlightRepository;
    private final NotificationRepository notificationRepository;

    public ResponseCommentDto getComments(RequestCommentWithIdsDto request, int page, int count) {
        UserEntity user = validateUser(request.getUserId());

        SectionEntity section = validateSection(request.getSectionId(), request.getRecordId(), request.getFolderId());
        validateFolderShare(section, user);

        HighlightEntity highlight = validateHighlight(request.getHighlightId());

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<CommentEntity> commentEntityPage = commentRepository.findByHighlightAndParentIsNullOrderByCreatedTimeDescCommentIdDesc(highlight, pageable);
        List<CommentEntity> parentCommentEntities = commentEntityPage.getContent();
        List<CommentDto> commentDtos = parentCommentEntities.stream()
                .map(CommentMapper.INSTANCE::toResponseCommentDto)
                .toList();
        long total = commentRepository.countByParentIsNullAndHighlight(highlight);

        return new ResponseCommentDto(commentDtos, total);
    }

    public ResponseReplyDto getReply(RequestCommentWithIdsDto request, int page, int count) {
        UserEntity user = validateUser(request.getUserId());

        SectionEntity section = validateSection(request.getSectionId(), request.getRecordId(), request.getFolderId());
        validateFolderShare(section, user);
        validateHighlight(request.getHighlightId());

        CommentEntity parent = commentRepository.findByCommentId(request.getParentId());
        if (parent == null) throw new CustomException(ErrorCode.NOT_FOUND_PARENT_COMMENT);

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<CommentEntity> commentEntityPage = commentRepository.findByParentOrderByCreatedTimeAscCommentIdAsc(parent, pageable);
        List<CommentEntity> commentEntities = commentEntityPage.getContent();
        List<ReplyDto> dto = commentEntities.stream()
                .map(CommentMapper.INSTANCE::toResponseReplyDto)
                .toList();

        return new ResponseReplyDto(dto, commentEntityPage.getTotalElements());
    }

    @Transactional
    public long addFirstComment(RequestCommentWithIdsDto request, RequestFirstCommentDto dto) {
        UserEntity user = validateUser(request.getUserId());

        SectionEntity section = validateSection(request.getSectionId(), request.getRecordId(), request.getFolderId());
        FolderShareEntity folderShare = validateFolderShareWithRole(section, user);

        Long isAlready = highlightRepository.existsBySectionAndInRange(section.getSectionId(), dto.getStartIdx(), dto.getEndIdx());
        if (isAlready == 1) throw new CustomException(ErrorCode.HIGHLIGHT_DUPLICATED);

        HighlightEntity createdHighlight = HighlightEntity.create(section, dto.getStartIdx(), dto.getEndIdx());
        HighlightEntity response = highlightRepository.save(createdHighlight);

        CommentEntity comment = CommentEntity.create(user, response, dto.getContent());
        CommentEntity addedComment = commentRepository.save(comment);

        NotificationEntity notification = NotificationMapper.INSTANCE.toNotificationWithFolderShareComment(
                new RequestNotificationWithFolderShareCommentDto(user, folderShare.getFolder(), folderShare, section.getRecord(),  addedComment)
        );
        notification.setTypeId(NotificationType.SHARE_FOLDER_ADD_COMMENT.getTypeId());
        notificationRepository.save(notification);

        notificationComment(user, folderShare);

        return response.getHighlightId();
    }

    @Transactional
    public void addComment(RequestCommentWithIdsDto request, RequestCommentDto dto) {
        UserEntity user = validateUser(request.getUserId());

        SectionEntity section = validateSection(request.getSectionId(), request.getRecordId(), request.getFolderId());
        FolderShareEntity folderShare = validateFolderShareWithRole(section, user);
        HighlightEntity highlight = validateHighlight(request.getHighlightId());

        CommentEntity comment = CommentEntity.create(user, highlight, dto.getContent());
        CommentEntity addedComment = commentRepository.save(comment);

        NotificationEntity notification = NotificationMapper.INSTANCE.toNotificationWithFolderShareComment(
                new RequestNotificationWithFolderShareCommentDto(user, folderShare.getFolder(), folderShare, section.getRecord(), addedComment)
        );
        notification.setTypeId(NotificationType.SHARE_FOLDER_ADD_COMMENT.getTypeId());
        notificationRepository.save(notification);

        notificationComment(user, folderShare);
    }

    @Transactional
    public void addReply(RequestCommentWithIdsDto request, RequestCommentDto dto) {
        UserEntity user = validateUser(request.getUserId());

        SectionEntity section = validateSection(request.getSectionId(), request.getRecordId(), request.getFolderId());
        FolderShareEntity folderShare = validateFolderShareWithRole(section, user);
        HighlightEntity highlight = validateHighlight(request.getHighlightId());

        CommentEntity parentComment = commentRepository.findByCommentId(request.getParentId());
        if (parentComment == null) throw new CustomException(ErrorCode.NOT_FOUND_PARENT_COMMENT);
        else if (parentComment.getParent() != null) throw new CustomException(ErrorCode.NOT_PARENT_COMMENT);

        CommentEntity comment = CommentEntity.createReply(user, highlight, parentComment, dto.getContent());
        CommentEntity addedComment = commentRepository.save(comment);

        NotificationEntity notification = NotificationMapper.INSTANCE.toNotificationWithFolderShareComment(
                new RequestNotificationWithFolderShareCommentDto(user, folderShare.getFolder(), folderShare, section.getRecord(), addedComment)
        );
        notification.setTypeId(NotificationType.SHARE_FOLDER_ADD_COMMENT.getTypeId());
        notificationRepository.save(notification);

        notificationComment(user, folderShare);
    }

    public void updateComment(RequestCommentWithIdsDto request, long commentId, RequestCommentDto dto) {
        UserEntity user = validateUser(request.getUserId());

        SectionEntity section = validateSection(request.getSectionId(), request.getRecordId(), request.getFolderId());
        validateFolderShareWithRole(section, user);
        validateHighlight(request.getHighlightId());
        CommentEntity comment = validateComment(commentId);

        if (!user.getUserId().equals(comment.getUser().getUserId())) throw new CustomException(ErrorCode.MISMATCH_COMMENT_OWNER);

        comment.setContent(dto.getContent());
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(RequestCommentWithIdsDto request, long commentId) {
        UserEntity user = validateUser(request.getUserId());

        SectionEntity section = validateSection(request.getSectionId(), request.getRecordId(), request.getFolderId());
        validateFolderShareWithRole(section, user);
        validateHighlight(request.getHighlightId());

        Long exists = commentRepository.existsSelfOrChildDeletedByCommentId(commentId);

        CommentEntity comment = validateComment(commentId);
        if (!user.getUserId().equals(comment.getUser().getUserId())) throw new CustomException(ErrorCode.MISMATCH_COMMENT_OWNER);

        if (exists == 1) {
            List<CommentEntity> deletedComments = commentRepository.findAllSelfOrChildDeletedByCommentId(commentId);
            if (deletedComments.isEmpty()) throw new CustomException(ErrorCode.NOT_FOUND_COMMENT);
            long highlightId = deletedComments.get(0).getHighlight().getHighlightId();

            commentRepository.deleteAll(deletedComments);

            HighlightEntity highlight = highlightRepository.findByHighlightId(highlightId);
            highlightRepository.delete(highlight);
        } else {
            comment.setDeleted(true);
            comment.setDeletedTime(new CustomTimestamp().getTimestamp());
            commentRepository.save(comment);
        }
    }

    private UserEntity validateUser(long userId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        return user;
    }

    public SectionEntity validateSection(long sectionId, long recordId, long folderId) {
        SectionEntity section = sectionRepository.findBySectionId(sectionId);
        if (section == null) throw new CustomException(ErrorCode.NOT_FOUND_SECTION);
        if (!section.getRecord().getRecordId().equals(recordId)) throw new CustomException(ErrorCode.NOT_FOUND_RECORD);
        if (!section.getRecord().getFolder().getFolderId().equals(folderId)) throw new CustomException(ErrorCode.NOT_FOUND_FOLDER);

        return section;
    }

    private FolderShareEntity validateFolderShare(SectionEntity section, UserEntity user) {
        FolderShareEntity folderShare = folderShareRepository.findByFolderAndTargetUser(section.getRecord().getFolder(), user);

        if (folderShare == null || folderShare.getInvitationStatus().equals(String.valueOf(InvitationStatus.PENDING)))
            throw new CustomException(ErrorCode.NOT_DESERVE_POST_COMMENT);

        return folderShare;
    }

    private FolderShareEntity validateFolderShareWithRole(SectionEntity section, UserEntity user) {
        FolderShareEntity folderShare = folderShareRepository.findByFolderAndTargetUser(section.getRecord().getFolder(), user);

        if (folderShare == null || folderShare.getInvitationStatus().equals(String.valueOf(InvitationStatus.PENDING)) || folderShare.getRoleId().getRoleId().equals("3"))
            throw new CustomException(ErrorCode.NOT_DESERVE_POST_COMMENT);

        return folderShare;
    }

    private HighlightEntity validateHighlight(long highlightId) {
        HighlightEntity highlight = highlightRepository.findByHighlightId(highlightId);
        if (highlight == null) throw new CustomException(ErrorCode.NOT_FOUND_HIGHLIGHT);

        return highlight;
    }

    private CommentEntity validateComment(long commentId) {
        CommentEntity comment = commentRepository.findByCommentId(commentId);
        if (comment == null) throw new CustomException(ErrorCode.NOT_FOUND_COMMENT);

        return comment;
    }

    private void notificationComment(UserEntity user, FolderShareEntity folderShareEntity) {
        List<FolderShareEntity> targetFolderShares = folderShareRepository.findAllByFolderFolderId(folderShareEntity.getFolder().getFolderId());
        if (targetFolderShares.isEmpty()) return;

        List<Message> messages = new ArrayList<>();

        try {
            for (FolderShareEntity targetFolderShare : targetFolderShares) {
                boolean isNotTarget = !targetFolderShare.getTargetUser().getShareNofi()
                        || targetFolderShare.getTargetUser().getNotificationToken() == null
                        || targetFolderShare.getTargetUser().getUserId().equals(user.getUserId());
                if (isNotTarget) {
                    continue;
                }

                Message message = Message.builder()
                        .setToken(targetFolderShare.getTargetUser().getNotificationToken())
                        .setNotification(Notification.builder()
                                .setTitle(user.getName() + "님이 댓글을 달았습니다!")
                                .build())
                        .build();
                messages.add(message);
            }

            firebaseMessaging.sendEach(messages, false);
        }  catch (Exception e) {
            log.debug("Failed to comment notification : " + folderShareEntity.getShareId() + " : " + e.getMessage());
        }
    }
}
