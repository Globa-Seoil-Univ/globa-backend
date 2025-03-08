package org.y2k2.globa.helper;

import org.y2k2.globa.dto.response.user.ResponseUserDto;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.type.SnsKind;

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
}
