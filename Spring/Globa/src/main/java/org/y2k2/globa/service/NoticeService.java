package org.y2k2.globa.service;

import com.google.cloud.storage.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.y2k2.globa.dto.NoticeAddRequestDto;
import org.y2k2.globa.entity.DummyImageEntity;
import org.y2k2.globa.entity.NoticeEntity;
import org.y2k2.globa.entity.NoticeImageEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.FileUploadException;
import org.y2k2.globa.exception.InvalidTokenException;
import org.y2k2.globa.exception.NotFoundException;
import org.y2k2.globa.mapper.NoticeMapper;
import org.y2k2.globa.repository.DummyImageRepository;
import org.y2k2.globa.repository.NoticeImageRepository;
import org.y2k2.globa.repository.NoticeRepository;
import org.y2k2.globa.repository.UserRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {
    public final UserRepository userRepository;
    public final NoticeRepository noticeRepostory;
    public final NoticeImageRepository noticeImageRepository;
    public final DummyImageRepository dummyImageRepository;

    @Autowired
    private Bucket bucket;

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

    @Transactional
    public Long addNotice(Long userId, NoticeAddRequestDto dto) {
        UserEntity user = userRepository.findByUserId(userId);

        if (user == null) {
            throw new InvalidTokenException("Invalid Token");
        }

        NoticeEntity requestEntity = NoticeMapper.INSTANCE.toEntity(dto);
        requestEntity.setUser(user);

        try {
            uploadThumbnail(requestEntity, dto.getThumbnail());
            NoticeEntity noticeEntity = noticeRepostory.save(requestEntity);

            if (dto.getImageIds() == null) return noticeEntity.getNoticeId();

            List<DummyImageEntity> dummyImageEntities = dummyImageRepository.findByImageIdIn(dto.getImageIds());
            List<NoticeImageEntity> noticeImageEntities = new ArrayList<>();

            if (dummyImageEntities.isEmpty()) return noticeEntity.getNoticeId();

            for (DummyImageEntity dummyImageEntity : dummyImageEntities) {
                noticeImageEntities.add(
                        NoticeImageEntity.create(
                                noticeEntity,
                                dummyImageEntity.getImagePath(),
                                dummyImageEntity.getImageSize(),
                                dummyImageEntity.getImageType()
                        )
                );
            }

            dummyImageRepository.deleteAll(dummyImageEntities);
            noticeImageRepository.saveAll(noticeImageEntities);

            return noticeEntity.getNoticeId();
        } catch (Exception e) {
            if (bucket.get(requestEntity.getThumbnailPath()) != null) {
                bucket.get(requestEntity.getThumbnailPath()).delete();
            }

            throw e;
        }
    }

    private void uploadThumbnail(NoticeEntity notice, MultipartFile file) {
        long current = new Date().getTime();
        long size = file.getSize();
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String mimeType = file.getContentType();

        String path = "notices/thumbnails/" + current + "." + extension;

        try {
            if (bucket.get(path) != null) {
                bucket.get(path).delete();
            }

            bucket.create(path, file.getBytes());

            notice.setThumbnailPath(path);
            notice.setThumbnailSize(size);
            notice.setThumbnailType(mimeType);
        } catch (Exception e) {
            if (bucket.get(path) != null) {
                bucket.get(path).delete();
            }

            throw new FileUploadException(e.getMessage());
        }
    }
}