package org.y2k2.globa.dto.request.folder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.y2k2.globa.annotation.EnumValue;
import org.y2k2.globa.type.Role;

import java.util.List;

@Getter
@NoArgsConstructor
public class RequestFolderPostDto {
    public record ShareTarget(
            @EnumValue(enumClass = Role.class, message = "R 또는 W만 가능합니다.")
            String role,
            @NotBlank(message = "사용자 코드는 필수입니다.")
            String code
    ) {}

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    private List<ShareTarget> shareTargets;
}
