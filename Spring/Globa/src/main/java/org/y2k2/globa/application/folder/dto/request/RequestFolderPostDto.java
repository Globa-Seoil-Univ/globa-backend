package org.y2k2.globa.application.folder.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.y2k2.globa.common.annotation.EnumValue;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestFolderPostDto {
    public record ShareTarget(
            @EnumValue(enumClass = FolderRole.class, message = "READER 또는 WRITER만 가능합니다.")
            String role,
            @Size(min = 6, max = 6, message = "사용자 코드는 6자리여야 합니다.")
            @NotBlank(message = "사용자 코드는 필수입니다.")
            String code
    ) {}

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @Valid
    private List<ShareTarget> shareTargets;
}
