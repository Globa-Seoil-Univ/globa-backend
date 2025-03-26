package org.y2k2.globa.helper;

import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.user.dto.response.ResponseNotificationSettingDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserSearchDto;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.domain.user.type.SnsKind;

public class UserHelper {
    public static UserEntity createUser() {
        UserEntity user = new UserEntity();
        user.setSnsId("1234567890");
        user.setSnsKind(SnsKind.KAKAO);
        user.setName("nickname");
        user.setCode("123456");
        user.setProfilePath("profilePath or firebasePath");
        user.setProfileType("image/jpeg");
        user.setProfileSize(12345L);
        user.setPrimaryNofi(false);
        user.setUploadNofi(false);
        user.setShareNofi(false);
        user.setEventNofi(false);
        user.setNotificationToken(null);
        user.setNotificationTokenTime(null);
        user.setIsDeleted(false);
        user.setDeletedTime(null);

        return user;
    }

    public static ResponseUserDto createResponseUserDto(UserEntity user) {
        ResponseUserDto responseUserDto = new ResponseUserDto();
        responseUserDto.setUserId(1L);
        responseUserDto.setProfile(user.getProfilePath());
        responseUserDto.setName(user.getName());
        responseUserDto.setCode(user.getCode());
        responseUserDto.setPublicFolderId(1L);

        return responseUserDto;
    }

    public static ResponseUserSearchDto createResponseUserSearchDto(UserEntity user) {
        ResponseUserSearchDto responseUserSearchDto = new ResponseUserSearchDto();
        responseUserSearchDto.setUserId(2L);
        responseUserSearchDto.setProfile(user.getProfilePath());
        responseUserSearchDto.setName(user.getName());
        responseUserSearchDto.setCode(user.getCode());

        return responseUserSearchDto;
    }

    public static ResponseNotificationSettingDto createResponseNotificationSettingDto(UserEntity user) {
        return ResponseNotificationSettingDto
                .builder()
                .uploadNofi(user.getUploadNofi())
                .shareNofi(user.getShareNofi())
                .eventNofi(user.getEventNofi())
                .build();
    }

    public static ResponseAnalysisDto createResponseAnalysisDto(UserEntity user) {
        return ResponseAnalysisDto
                .builder()
                .userId(user.getUserId())
                .profile(user.getProfilePath())
                .name(user.getName())
                .code(user.getCode())
                .build();
    }
}
