package org.y2k2.globa.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /* 400 BAD_REQUEST : 잘못된 요청 */
    EXPIRED_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "Access Token Expired ! ", 40010),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Request Value BAD ! ", 400),
    FOLDER_DELETE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Default Folder Cannot Be Deleted ", 40020),

    /* 401 UNAUTHORIZED : 허락되지 않은 사용자 */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Access Token Invalid", 40110),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,  "Refresh Token Expired ! ", 40130),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized error ! ", 401),

    /* 403 FORBIDDEN : 잘못된 접근 */
    FORBIDDEN(HttpStatus.FORBIDDEN, "BAD ACCESS", 403),

    /* 404 NOT_FOUND : 값이 없음 */
    NOT_FOUND(HttpStatus.NOT_FOUND, " NOT FOUND ", 404),

    /* 409 CONFLICT : 충돌 */
    HIGHLIGHT_DUPLICATED(HttpStatus.CONFLICT, "Comment Highlight Already Exist ! ", 40910),
    FOLDER_NAME_DUPLICATED(HttpStatus.CONFLICT, "Folder Name Duplicated ! ", 40920),


    /* 500 SERVER ERROR : 서버 단 에러 */
    FAILED_FILE_UPLOAD(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to file upload to Firebase ! ",50010),
    FAILED_FCM_SEND(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send FCM message !", 50020),
    REDIS_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS TIMEOUT ! ", 50030),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final int errorCode;
}
