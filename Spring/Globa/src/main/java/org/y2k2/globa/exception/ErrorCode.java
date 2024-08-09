package org.y2k2.globa.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /* 400 BAD_REQUEST : 잘못된 요청 */
    EXPIRED_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "Access Token Expired ! ", 40010),
    ACTIVE_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "Refresh Token Not Expired ! ", 40011),
    NOT_MATCH_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "Not Equal Refresh Token ! ", 40012),
    REQUIRED_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "Must Be Requested To Access Token ! ", 40013),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Request Value BAD ! ", 400),
    FOLDER_DELETE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Default Folder Cannot Be Deleted ", 40020),
    DELETED_USER(HttpStatus.BAD_REQUEST, "User Deleted ! ", 40030),
    REQUIRED_FOLDER_TITLE(HttpStatus.BAD_REQUEST, "Must Be Requested To Folder Title ", 40040),
    REQUIRED_FOLDER_ID(HttpStatus.BAD_REQUEST, "Must Be Requested To Folder Id ", 40041),
    REQUIRED_NOTICE_ID(HttpStatus.BAD_REQUEST, "Must Be Requested To Notice Id ", 40050),
    NOT_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "Parent id exists in the requested comment id ", 40060),

    /* 401 UNAUTHORIZED : 허락되지 않은 사용자 */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Access Token Invalid", 40110),
    SIGNATURE(HttpStatus.UNAUTHORIZED, "Not Matched Token", 40111),
    NOT_DESERVE_FOLDER(HttpStatus.UNAUTHORIZED, "Not Deserve for Folder", 40120),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,  "Refresh Token Expired ! ", 40130),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized error ! ", 401),

    /* 403 FORBIDDEN : 잘못된 접근 */
    FORBIDDEN(HttpStatus.FORBIDDEN, "BAD ACCESS", 403),
    MISMATCH_INQUIRY_OWNER(HttpStatus.FORBIDDEN, "This Inquiry isn't Your Own", 40310),
    NOT_DESERVE_ADD_NOTICE(HttpStatus.FORBIDDEN,"Only admin or editor can write answers",40320),
    REQUIRED_ROLE(HttpStatus.FORBIDDEN,"Wrong Approach",40430),
    MISMATCH_FOLDER_OWNER(HttpStatus.FORBIDDEN, "This Folder isn't Your Own", 40340),
    MISMATCH_COMMENT_OWNER(HttpStatus.FORBIDDEN, "This Comment isn't Your Own", 40350),

    /* 404 NOT_FOUND : 값이 없음 */
    NOT_FOUND(HttpStatus.NOT_FOUND, " NOT FOUND ", 404),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, " Not Found User ! ", 40410),
    NOT_FOUND_DEFAULT_FOLDER(HttpStatus.NOT_FOUND, " Not Found Default Folder ! ", 40411),
    NOT_FOUND_INQUIRY(HttpStatus.NOT_FOUND, " Not Found Inquiry ! ", 40420),
    NOT_FOUND_NOTICE(HttpStatus.NOT_FOUND, " Not Found Notice ! ", 40430),
    NOT_FOUND_ANSWER(HttpStatus.NOT_FOUND, " Not Found ANSWER ! ", 40440),
    NOT_FOUND_FOLDER(HttpStatus.NOT_FOUND, " Not Found Folder ! ", 40450),
    NOT_FOUND_HIGHLIGHT(HttpStatus.NOT_FOUND, " Not Found Highlight ! ", 40460),
    NOT_FOUND_PARENT_COMMENT(HttpStatus.NOT_FOUND, " Not Found Parent Comment ! ", 40470),


    /* 409 CONFLICT : 충돌 */
    HIGHLIGHT_DUPLICATED(HttpStatus.CONFLICT, "Comment Highlight Already Exist ! ", 40910),
    FOLDER_NAME_DUPLICATED(HttpStatus.CONFLICT, "Folder Name Duplicated ! ", 40920),
    INQUIRY_ANSWER_DUPLICATED(HttpStatus.CONFLICT, "Answer Duplicated ! ", 40930),

    /* 500 SERVER ERROR : 서버 단 에러 */
    FAILED_FILE_UPLOAD(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to file upload to Firebase ! ",50010),
    FAILED_FCM_SEND(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send FCM message !", 50020),
    REDIS_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS TIMEOUT ! ", 50030),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final int errorCode;
}
