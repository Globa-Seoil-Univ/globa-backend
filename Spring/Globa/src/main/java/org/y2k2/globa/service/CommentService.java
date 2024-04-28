package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.y2k2.globa.dto.*;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.mapper.CommentMapper;
import org.y2k2.globa.repository.*;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final FolderShareRepository folderShareRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final HighlightRepository highlightRepository;

    public ResponseCommentDto getComments(RequestCommentWithIdsDto request, int page, int count) {
        UserEntity user = validateUser(request.getUserId());
        SectionEntity section = validateSection(request.getSectionId(), request.getRecordId(), request.getFolderId());
        validateFolderShare(section, user);

        HighlightEntity highlight = highlightRepository.findByHighlightId(request.getHighlightId());
        if (highlight == null) throw new NotFoundException("Not found highlight");

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

        HighlightEntity highlight = highlightRepository.findByHighlightId(request.getHighlightId());
        if (highlight == null) throw new NotFoundException("Not found highlight");

        CommentEntity parent = commentRepository.findByCommentId(request.getParentId());
        if (parent == null) throw new NotFoundException("Not found parent comment");

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
        validateFolderShare(section, user);

        // 같은 인덱스에 있는 곳에 댓글을 추가할라면 리턴 
        HighlightEntity highlight = highlightRepository.findBySectionAndStartIndexAndEndIndex(section, dto.getStartIdx(), dto.getEndIdx());
        if (highlight != null) throw new DuplicatedExcepiton("Already highlight");

        HighlightEntity createdHighlight = HighlightEntity.create(section, dto.getStartIdx(), dto.getEndIdx());
        HighlightEntity response = highlightRepository.save(createdHighlight);

        CommentEntity comment = CommentEntity.create(user, response, dto.getContent());
        commentRepository.save(comment);

        return response.getHighlightId();
    }

    @Transactional
    public void addComment(RequestCommentWithIdsDto request, RequestCommentDto dto) {
        UserEntity user = validateUser(request.getUserId());
        SectionEntity section = validateSection(request.getSectionId(), request.getRecordId(), request.getFolderId());
        validateFolderShare(section, user);

        HighlightEntity highlight = highlightRepository.findByHighlightId(request.getHighlightId());
        if (highlight == null) throw new NotFoundException("Not found highlight");

        CommentEntity comment = CommentEntity.create(user, highlight, dto.getContent());
        commentRepository.save(comment);
    }

    @Transactional
    public void addReply(RequestCommentWithIdsDto request, RequestCommentDto dto) {
        UserEntity user = validateUser(request.getUserId());
        SectionEntity section = validateSection(request.getSectionId(), request.getRecordId(), request.getFolderId());
        validateFolderShare(section, user);

        HighlightEntity highlight = highlightRepository.findByHighlightId(request.getHighlightId());
        if (highlight == null) throw new NotFoundException("Not found highlight");

        CommentEntity parentComment = commentRepository.findByCommentId(request.getParentId());
        if (parentComment == null) throw new NotFoundException("Not found parent comment");
        else if (parentComment.getParent() != null) throw new BadRequestException("Parent id exists in the requested comment id");

        CommentEntity comment = CommentEntity.createReply(user, highlight, parentComment, dto.getContent());
        commentRepository.save(comment);
    }

    public UserEntity validateUser(long userId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new InvalidTokenException("Invalid Token");

        return user;
    }

    public SectionEntity validateSection(long sectionId, long recordId, long folderId) {
        SectionEntity section = sectionRepository.findBySectionId(sectionId);
        if (section == null) throw new NotFoundException("Not found section");
        if (!section.getRecord().getRecordId().equals(recordId)) throw new NotFoundException("Not found record");
        if (!section.getRecord().getFolder().getFolderId().equals(folderId)) throw new NotFoundException("Not found folder");

        return section;
    }

    public void validateFolderShare(SectionEntity section, UserEntity user) {
        FolderShareEntity folderShare = folderShareRepository.findByFolderAndTargetUser(section.getRecord().getFolder(), user);
        if (folderShare == null) throw new ForbiddenException("You aren't authorized to post comments");
    }
}
