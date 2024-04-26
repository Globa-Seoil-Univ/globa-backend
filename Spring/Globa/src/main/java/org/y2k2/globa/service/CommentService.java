package org.y2k2.globa.service;

import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.y2k2.globa.dto.RequestCommentDto;
import org.y2k2.globa.dto.RequestFirstCommentDto;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.DuplicatedExcepiton;
import org.y2k2.globa.exception.ForbiddenException;
import org.y2k2.globa.exception.InvalidTokenException;
import org.y2k2.globa.exception.NotFoundException;
import org.y2k2.globa.repository.*;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentService {
    private EntityManager entityManager;
    private final CommentRepository commentRepository;
    private final FolderRepository folderRepository;
    private final FolderShareRepository folderShareRepository;
    private final RecordRepository recordRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final HighlightRepository highlightRepository;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CommentRequest {
        private long userId;
        private long folderId;
        private long recordId;
        private long sectionId;
    }

    @Transactional
    public Long addFirstComment(CommentRequest request, RequestFirstCommentDto dto) {
        UserEntity user = userRepository.findByUserId(request.getUserId());
        if (user == null) throw new InvalidTokenException("Invalid Token");

        SectionEntity section = sectionRepository.findBySectionId(request.getSectionId());

        if (section == null) throw new NotFoundException("Not found record");
        if (!section.getRecord().getRecordId().equals(request.getRecordId())) throw new NotFoundException("Not found record");
        if (!section.getRecord().getFolder().getFolderId().equals(request.getFolderId())) throw new NotFoundException("Not found folder");

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
    public void addComment(CommentRequest request, long highlightId, RequestCommentDto dto) {
        UserEntity user = userRepository.findByUserId(request.getUserId());
        if (user == null) throw new InvalidTokenException("Invalid Token");

        SectionEntity section = sectionRepository.findBySectionId(request.getSectionId());

        if (section == null) throw new NotFoundException("Not found section");
        if (!section.getRecord().getRecordId().equals(request.getRecordId())) throw new NotFoundException("Not found record");
        if (!section.getRecord().getFolder().getFolderId().equals(request.getFolderId())) throw new NotFoundException("Not found folder");

        FolderShareEntity folderShare = folderShareRepository.findByFolderAndTargetUser(section.getRecord().getFolder(), user);
        if (folderShare == null) throw new ForbiddenException("You aren't authorized to post comments");

        HighlightEntity highlight = highlightRepository.findByHighlightId(highlightId);
        if (highlight == null) throw new NotFoundException("Not found highlight");

        CommentEntity comment = CommentEntity.create(user, highlight, dto.getContent());
        commentRepository.save(comment);
    }
}
