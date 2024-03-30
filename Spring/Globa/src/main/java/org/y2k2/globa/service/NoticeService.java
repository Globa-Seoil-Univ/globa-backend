package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import org.y2k2.globa.entity.NoticeEntity;
import org.y2k2.globa.exception.NotFoundException;
import org.y2k2.globa.repository.NoticeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {
    public final NoticeRepository noticeRepostory;

    public List<NoticeEntity> getIntroNotices() {
        return noticeRepostory.findByOrderByCreatedTimeDesc(Limit.of(3));
    }

    public NoticeEntity getNoticeDetail(Long noticeId) {
        NoticeEntity noticeEntity = noticeRepostory.findByNoticeId(noticeId);

        if (noticeEntity == null) {
            throw new NotFoundException("Not found notice using noticeId : " + noticeId);
        }

        return noticeEntity;
    }

//    public ObjectNode insertNotice() {
//        NoticeEntity noticeEntity = new NoticeEntity();
//        noticeEntity.set
//    }

}
