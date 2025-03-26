package org.y2k2.globa.application.dummyimage.dto.request;

import org.springframework.web.multipart.MultipartFile;
import org.y2k2.globa.common.annotation.ValidFile;

public record RequestDummyImageDto(
        @ValidFile
        MultipartFile image
) {
}
