package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.y2k2.globa.dto.common.comment.CommentDto;
import org.y2k2.globa.dto.common.comment.ReplyDto;
import org.y2k2.globa.dto.request.notification.RequestNotificationWithFolderShareCommentDto;
import org.y2k2.globa.dto.request.comment.RequestCommentDto;
import org.y2k2.globa.dto.request.comment.RequestCommentWithIdsDto;
import org.y2k2.globa.dto.request.comment.RequestFirstCommentDto;
import org.y2k2.globa.dto.response.comment.ResponseCommentDto;
import org.y2k2.globa.dto.response.comment.ResponseReplyDto;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.mapper.CommentMapper;
import org.y2k2.globa.type.InvitationStatus;
import org.y2k2.globa.type.NotificationType;
import org.y2k2.globa.util.CustomTimestamp;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final NotificationService notificationService;
    private final ApplicationEventPublisher publisher;

    private final CommentRepository commentRepository;
    private final FolderShareRepository folderShareRepository;
    private final SectionRepository sectionRepository;
    private final HighlightRepository highlightRepository;

    public ResponseCommentDto getComments(RequestCommentWithIdsDto request, int page, int count) {
        SectionEntity section = sectionRepository.findBySection(request.sectionId(), request.folderId(), request.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));
        validateFolderShare(section, request.user());
        HighlightEntity highlight = validateHighlight(request.highlightId());

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<CommentEntity> commentEntityPage = commentRepository.findByHighlightAndParentIsNullOrderByCommentIdDesc(highlight, pageable);

        List<CommentEntity> parentCommentEntities = commentEntityPage.getContent();
        List<CommentDto> comments = parentCommentEntities.stream()
                .map(CommentMapper.INSTANCE::toResponseCommentDto)
                .toList();

        return new ResponseCommentDto(comments, commentEntityPage.getTotalElements());
    }

    public ResponseReplyDto getReply(RequestCommentWithIdsDto request, int page, int count) {
        SectionEntity section = sectionRepository.findBySection(request.sectionId(), request.folderId(), request.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));

        validateFolderShare(section, request.user());
        validateHighlight(request.highlightId());

        CommentEntity parentComment = commentRepository.findByCommentIdAndParentIsNull(request.parentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PARENT_COMMENT));

        if (!parentComment.getHighlight().getHighlightId().equals(request.highlightId())) {
            throw new CustomException(ErrorCode.NOT_INCLUDE_HIGHLIGHT_COMMENT);
        }

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<CommentEntity> commentEntityPage = commentRepository.findByParent_CommentIdOrderByCommentIdAsc(request.parentId(), pageable);
        List<CommentEntity> comments = commentEntityPage.getContent();
        List<ReplyDto> dto = comments.stream()
                .map(CommentMapper.INSTANCE::toResponseReplyDto)
                .toList();

        return new ResponseReplyDto(dto, commentEntityPage.getTotalElements());
    }

    @Transactional
    public long addFirstComment(RequestCommentWithIdsDto request, RequestFirstCommentDto dto) {
        SectionEntity section = sectionRepository.findBySection(request.sectionId(), request.folderId(), request.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));
        FolderShareEntity folderShare = validateFolderShareWithRole(section, request.user());

        boolean isAlready = highlightRepository.existsBySectionAndInRange(section.getSectionId(), dto.startIdx(), dto.endIdx());
        if (isAlready) throw new CustomException(ErrorCode.HIGHLIGHT_DUPLICATED);

        HighlightEntity highlight = HighlightEntity.create(section, dto.startIdx(), dto.endIdx());
        HighlightEntity createdHighlight = highlightRepository.save(highlight);

        CommentEntity comment = saveComment(request.user(), createdHighlight, dto.content());
        saveNotification(request, folderShare, section, comment);
        sendNotification(request.user(), section.getRecord().getFolder(), section.getRecord(), folderShare);

        return createdHighlight.getHighlightId();
    }

    @Transactional
    public void addComment(RequestCommentWithIdsDto request, RequestCommentDto dto) {
        SectionEntity section = sectionRepository.findBySection(request.sectionId(), request.folderId(), request.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));
        FolderShareEntity folderShare = validateFolderShareWithRole(section, request.user());
        HighlightEntity highlight = validateHighlight(request.highlightId());

        CommentEntity comment = saveComment(request.user(), highlight, dto.getContent());
        saveNotification(request, folderShare, section, comment);
        sendNotification(request.user(), section.getRecord().getFolder(), section.getRecord(), folderShare);
    }

    @Transactional
    public void addReply(RequestCommentWithIdsDto request, RequestCommentDto dto) {
        SectionEntity section = sectionRepository.findBySection(request.sectionId(), request.folderId(), request.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));
        FolderShareEntity folderShare = validateFolderShareWithRole(section, request.user());
        HighlightEntity highlight = validateHighlight(request.highlightId());

        CommentEntity parentComment = commentRepository.findByCommentIdAndParentIsNull(request.parentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PARENT_COMMENT));

        CommentEntity addedComment = saveComment(request.user(), highlight, parentComment, dto.getContent());
        saveNotification(request, folderShare, section, addedComment);
        sendNotification(request.user(), section.getRecord().getFolder(), section.getRecord(), folderShare);
    }

    public void updateComment(RequestCommentWithIdsDto request, long commentId, RequestCommentDto dto) {
        SectionEntity section = sectionRepository.findBySection(request.sectionId(), request.folderId(), request.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));
        validateFolderShareWithRole(section, request.user());
        validateHighlight(request.highlightId());
        CommentEntity comment = validateComment(commentId, request.user().getUserId());

        comment.setContent(dto.getContent());
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(RequestCommentWithIdsDto request, Long commentId) {
        SectionEntity section = sectionRepository.findBySection(request.sectionId(), request.folderId(), request.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));
        validateFolderShareWithRole(section, request.user());
        validateHighlight(request.highlightId());

        Boolean exists = commentRepository.existsSelfOrChildDeletedByCommentId(commentId);
        CommentEntity comment = validateComment(commentId, request.user().getUserId());

        if (exists) {
            List<CommentEntity> deletedComments = commentRepository.findAllSelfOrChildDeletedByCommentId(commentId);
            if (deletedComments.isEmpty()) throw new CustomException(ErrorCode.NOT_FOUND_COMMENT);

            Long highlightId = deletedComments.get(0).getHighlight().getHighlightId();
            HighlightEntity highlight = highlightRepository.findByHighlightId(highlightId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_HIGHLIGHT));

            commentRepository.deleteAllInBatch(deletedComments);
            highlightRepository.delete(highlight);
        } else {
            comment.setIsDeleted(true);
            comment.setDeletedTime(new CustomTimestamp().getTimestamp());
            commentRepository.save(comment);
        }
    }

    private void validateFolderShare(SectionEntity section, UserEntity user) {
        FolderShareEntity folderShare = folderShareRepository.findByFolderAndTargetUser(section.getRecord().getFolder(), user)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_DESERVE_POST_COMMENT));

        if (folderShare.getInvitationStatus().equals(InvitationStatus.PENDING))
            throw new CustomException(ErrorCode.NOT_DESERVE_POST_COMMENT);
    }

    private FolderShareEntity validateFolderShareWithRole(SectionEntity section, UserEntity user) {
        FolderShareEntity folderShare = folderShareRepository.findByFolderAndTargetUser(section.getRecord().getFolder(), user)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_DESERVE_POST_COMMENT));

        // TODO : Role Enum으로 변경
        // TODO : Swagger 업데이트
        if (folderShare.getInvitationStatus().equals(InvitationStatus.PENDING) || folderShare.getRole().getRoleId().equals("3"))
            throw new CustomException(ErrorCode.NOT_DESERVE_POST_COMMENT);

        return folderShare;
    }

    private HighlightEntity validateHighlight(long highlightId) {
        return highlightRepository.findByHighlightId(highlightId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_HIGHLIGHT));
    }

    private CommentEntity validateComment(Long commentId, Long userId) {
        CommentEntity comment = commentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));
        if (!comment.getUser().getUserId().equals(userId)) throw new CustomException(ErrorCode.MISMATCH_COMMENT_OWNER);

        return comment;
    }

    private CommentEntity saveComment(UserEntity user, HighlightEntity highlight, String content) {
        CommentEntity comment = CommentEntity.create(user, highlight, content);
        return commentRepository.save(comment);
    }

    private CommentEntity saveComment(UserEntity user, HighlightEntity highlight, CommentEntity parent, String content) {
        CommentEntity comment = CommentEntity.createReply(user, highlight, parent, content);
        return commentRepository.save(comment);
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
        List<FolderShareEntity> targetFolderShares = folderShareRepository.findAllByFolderFolderId(folderShareEntity.getFolder().getFolderId());
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
