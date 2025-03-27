package org.y2k2.globa.application.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.comment.repository.CommentJpaRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.repository.HighlightJpaRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.section.repository.SectionJpaRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.application.comment.dto.common.CommentDto;
import org.y2k2.globa.application.comment.dto.common.ReplyDto;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithFolderShareCommentDto;
import org.y2k2.globa.application.comment.dto.request.RequestCommentDto;
import org.y2k2.globa.application.comment.dto.request.RequestCommentWithIdsDto;
import org.y2k2.globa.application.comment.dto.request.RequestFirstCommentDto;
import org.y2k2.globa.application.comment.dto.response.ResponseCommentDto;
import org.y2k2.globa.application.comment.dto.response.ResponseReplyDto;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.infrastructure.persistence.foldershare.repository.FolderShareJpaRepository;
import org.y2k2.globa.application.comment.mapper.CommentMapper;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.common.type.FolderRole;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.application.notification.service.NotificationService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final NotificationService notificationService;
    private final ApplicationEventPublisher publisher;

    private final CommentJpaRepository commentJpaRepository;
    private final FolderShareJpaRepository folderShareJpaRepository;
    private final SectionJpaRepository sectionJpaRepository;
    private final HighlightJpaRepository highlightJpaRepository;

    public ResponseCommentDto getComments(RequestCommentWithIdsDto request, int page, int count) {
        SectionEntity section = sectionJpaRepository.findBySection(request.sectionId(), request.folderId(), request.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));
        validateFolderShare(section, request.user());
        HighlightEntity highlight = validateHighlight(request.highlightId());

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<CommentEntity> commentEntityPage = commentJpaRepository.findByHighlightAndParentIsNullOrderByCommentIdDesc(highlight, pageable);

        List<CommentEntity> parentCommentEntities = commentEntityPage.getContent();
        List<CommentDto> comments = parentCommentEntities.stream()
                .map(CommentMapper.INSTANCE::toResponseCommentDto)
                .toList();

        return new ResponseCommentDto(comments, commentEntityPage.getTotalElements());
    }

    public ResponseReplyDto getReply(RequestCommentWithIdsDto request, int page, int count) {
        SectionEntity section = sectionJpaRepository.findBySection(request.sectionId(), request.folderId(), request.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));

        validateFolderShare(section, request.user());
        validateHighlight(request.highlightId());

        CommentEntity parentComment = commentJpaRepository.findByCommentIdAndParentIsNull(request.parentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PARENT_COMMENT));

        if (!parentComment.getHighlight().getHighlightId().equals(request.highlightId())) {
            throw new CustomException(ErrorCode.NOT_INCLUDE_HIGHLIGHT_COMMENT);
        }

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<CommentEntity> commentEntityPage = commentJpaRepository.findByParent_CommentIdOrderByCommentIdAsc(request.parentId(), pageable);
        List<CommentEntity> comments = commentEntityPage.getContent();
        List<ReplyDto> dto = comments.stream()
                .map(CommentMapper.INSTANCE::toResponseReplyDto)
                .toList();

        return new ResponseReplyDto(dto, commentEntityPage.getTotalElements());
    }

    @Transactional
    public long addFirstComment(RequestCommentWithIdsDto request, RequestFirstCommentDto dto) {
        SectionEntity section = sectionJpaRepository.findBySection(request.sectionId(), request.folderId(), request.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));
        FolderShareEntity folderShare = validateFolderShareWithRole(section, request.user());

        boolean isAlready = highlightJpaRepository.existsBySectionAndInRange(section.getSectionId(), dto.startIdx(), dto.endIdx());
        if (isAlready) throw new CustomException(ErrorCode.HIGHLIGHT_DUPLICATED);

        HighlightEntity highlight = HighlightEntity.create(section, dto.startIdx(), dto.endIdx());
        HighlightEntity createdHighlight = highlightJpaRepository.save(highlight);

        CommentEntity comment = saveComment(request.user(), createdHighlight, dto.content());
        saveNotification(request, folderShare, section, comment);
        sendNotification(request.user(), section.getRecord().getFolder(), section.getRecord(), folderShare);

        return createdHighlight.getHighlightId();
    }

    @Transactional
    public void addComment(RequestCommentWithIdsDto request, RequestCommentDto dto) {
        SectionEntity section = sectionJpaRepository.findBySection(request.sectionId(), request.folderId(), request.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));
        FolderShareEntity folderShare = validateFolderShareWithRole(section, request.user());
        HighlightEntity highlight = validateHighlight(request.highlightId());

        CommentEntity comment = saveComment(request.user(), highlight, dto.getContent());
        saveNotification(request, folderShare, section, comment);
        sendNotification(request.user(), section.getRecord().getFolder(), section.getRecord(), folderShare);
    }

    @Transactional
    public void addReply(RequestCommentWithIdsDto request, RequestCommentDto dto) {
        SectionEntity section = sectionJpaRepository.findBySection(request.sectionId(), request.folderId(), request.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));
        FolderShareEntity folderShare = validateFolderShareWithRole(section, request.user());
        HighlightEntity highlight = validateHighlight(request.highlightId());

        CommentEntity parentComment = commentJpaRepository.findByCommentIdAndParentIsNull(request.parentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PARENT_COMMENT));

        CommentEntity addedComment = saveComment(request.user(), highlight, parentComment, dto.getContent());
        saveNotification(request, folderShare, section, addedComment);
        sendNotification(request.user(), section.getRecord().getFolder(), section.getRecord(), folderShare);
    }

    public void updateComment(RequestCommentWithIdsDto request, long commentId, RequestCommentDto dto) {
        SectionEntity section = sectionJpaRepository.findBySection(request.sectionId(), request.folderId(), request.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));
        validateFolderShareWithRole(section, request.user());
        validateHighlight(request.highlightId());
        CommentEntity comment = validateComment(commentId, request.user().getUserId());

        comment.setContent(dto.getContent());
        commentJpaRepository.save(comment);
    }

    @Transactional
    public void deleteComment(RequestCommentWithIdsDto request, Long commentId) {
        SectionEntity section = sectionJpaRepository.findBySection(request.sectionId(), request.folderId(), request.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));
        validateFolderShareWithRole(section, request.user());
        validateHighlight(request.highlightId());

        Boolean exists = commentJpaRepository.existsSelfOrChildDeletedByCommentId(commentId);
        CommentEntity comment = validateComment(commentId, request.user().getUserId());

        if (exists) {
            List<CommentEntity> deletedComments = commentJpaRepository.findAllSelfOrChildDeletedByCommentId(commentId);
            if (deletedComments.isEmpty()) throw new CustomException(ErrorCode.NOT_FOUND_COMMENT);

            Long highlightId = deletedComments.get(0).getHighlight().getHighlightId();
            HighlightEntity highlight = highlightJpaRepository.findByHighlightId(highlightId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_HIGHLIGHT));

            commentJpaRepository.deleteAllInBatch(deletedComments);
            highlightJpaRepository.delete(highlight);
        } else {
            comment.setIsDeleted(true);
            comment.setDeletedTime(new CustomTimestamp().getTimestamp());
            commentJpaRepository.save(comment);
        }
    }

    private void validateFolderShare(SectionEntity section, UserEntity user) {
        FolderShareEntity folderShare = folderShareJpaRepository.findByFolderAndTargetUser(section.getRecord().getFolder(), user)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER));

        if (folderShare.getInvitationStatus().equals(InvitationStatus.PENDING))
            throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);
    }

    private FolderShareEntity validateFolderShareWithRole(SectionEntity section, UserEntity user) {
        FolderShareEntity folderShare = folderShareJpaRepository.findByFolderAndTargetUserJoinRole(section.getRecord().getFolder(), user)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_DESERVE_POST_COMMENT));

        if (folderShare.getInvitationStatus().equals(InvitationStatus.PENDING) || folderShare.getRole().getRoleName().equals(FolderRole.READER.getRoleName()))
            throw new CustomException(ErrorCode.NOT_DESERVE_POST_COMMENT);

        return folderShare;
    }

    private HighlightEntity validateHighlight(long highlightId) {
        return highlightJpaRepository.findByHighlightId(highlightId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_HIGHLIGHT));
    }

    private CommentEntity validateComment(Long commentId, Long userId) {
        CommentEntity comment = commentJpaRepository.findByCommentId(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));
        if (!comment.getUser().getUserId().equals(userId)) throw new CustomException(ErrorCode.MISMATCH_COMMENT_OWNER);

        return comment;
    }

    private CommentEntity saveComment(UserEntity user, HighlightEntity highlight, String content) {
        CommentEntity comment = CommentEntity.create(user, highlight, content);
        return commentJpaRepository.save(comment);
    }

    private CommentEntity saveComment(UserEntity user, HighlightEntity highlight, CommentEntity parent, String content) {
        CommentEntity comment = CommentEntity.createReply(user, highlight, parent, content);
        return commentJpaRepository.save(comment);
    }

    private void saveNotification(
            RequestCommentWithIdsDto request,
            FolderShareEntity folderShare,
            SectionEntity section,
            CommentEntity comment
    ) {
        RequestNotificationWithFolderShareCommentDto info = RequestNotificationWithFolderShareCommentDto.builder()
                .sender(request.user())
                .title(request.user().getName() + "님이 댓글을 달았습니다!")
                .folder(section.getRecord().getFolder())
                .folderShare(folderShare)
                .record(section.getRecord())
                .comment(comment)
                .notificationType(NotificationType.SHARE_FOLDER_ADD_COMMENT)
                .build();

        notificationService.saveNotification(info);
    }

    private void sendNotification(UserEntity user, FolderEntity folder, RecordEntity record, FolderShareEntity folderShareEntity) {
        List<FolderShareEntity> targetFolderShares = folderShareJpaRepository.findAllByFolderFolderId(folderShareEntity.getFolder().getFolderId());
        List<RequestNotificationWithFolderShareCommentDto> infos = targetFolderShares.stream()
                .map(targetFolderShare -> RequestNotificationWithFolderShareCommentDto.builder()
                        .sender(user)
                        .title(user.getName() + "님이 댓글을 달았습니다!")
                        .folder(folder)
                        .folderShare(targetFolderShare)
                        .record(record)
                        .notificationType(NotificationType.SHARE_FOLDER_ADD_COMMENT)
                        .build())
                .collect(Collectors.toList());

        publisher.publishEvent(infos);
    }
}
