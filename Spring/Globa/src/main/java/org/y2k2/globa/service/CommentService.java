package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.y2k2.globa.dto.RequestFirstCommentDto;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.DuplicatedExcepiton;
import org.y2k2.globa.exception.InvalidTokenException;
import org.y2k2.globa.exception.NotFoundException;
import org.y2k2.globa.repository.*;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final FolderRepository folderRepository;
    private final RecordRepository recordRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final HighlightRepository highlightRepository;

    @Transactional
    public Long addFirstComment(Long userId, Long folderId, Long recordId, Long sectionId, RequestFirstCommentDto dto) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new InvalidTokenException("Invalid Token");

        SectionEntity section = sectionRepository.findBySectionId(sectionId);

        if (section == null) throw new NotFoundException("Not found record");
        if (!section.getRecord().getRecordId().equals(recordId)) throw new NotFoundException("Not found record");
        if (!section.getRecord().getFolder().getFolderId().equals(folderId)) throw new NotFoundException("Not found folder");

        // 같은 인덱스에 있는 곳에 댓글을 추가할라면 리턴 
        HighlightEntity highlight = highlightRepository.findBySectionAndStartIndexAndEndIndex(section, dto.getStartIdx(), dto.getEndIdx());
        if (highlight != null) throw new DuplicatedExcepiton("Already highlight");

        HighlightEntity createdHighlight = HighlightEntity.create(section, dto.getStartIdx(), dto.getEndIdx());
        HighlightEntity response = highlightRepository.save(createdHighlight);

        CommentEntity comment = CommentEntity.create(user, response, dto.getContent());
        commentRepository.save(comment);

        return response.getHighlightId();
    }
}
