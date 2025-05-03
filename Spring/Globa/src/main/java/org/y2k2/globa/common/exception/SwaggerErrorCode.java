package org.y2k2.globa.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SwaggerErrorCode {
    /* 400 BAD_REQUEST : 잘못된 요청 */
    public static final String EXPIRED_ACCESS_TOKEN = "EXPIRED_ACCESS_TOKEN";
    public static final String EXPIRED_ACCESS_TOKEN_VALUE = "40010";
    public static final String ACTIVE_ACCESS_TOKEN = "ACTIVE_ACCESS_TOKEN";
    public static final String ACTIVE_ACCESS_TOKEN_VALUE = "40011";
    public static final String NOT_MATCH_REFRESH_TOKEN = "NOT_MATCH_REFRESH_TOKEN";
    public static final String NOT_MATCH_REFRESH_TOKEN_VALUE = "40012";
    public static final String INVITE_BAD_REQUEST = "INVITE_BAD_REQUEST";
    public static final String INVITE_BAD_REQUEST_VALUE = "40020";
    public static final String INVITE_ACCEPT_BAD_REQUEST = "INVITE_ACCEPT_BAD_REQUEST";
    public static final String INVITE_ACCEPT_BAD_REQUEST_VALUE = "40021";
    public static final String NOT_INCLUDE_HIGHLIGHT_COMMENT = "NOT_INCLUDE_HIGHLIGHT_COMMENT";
    public static final String NOT_INCLUDE_HIGHLIGHT_COMMENT_VALUE = "40030";
    public static final String MISMATCH_FOLDER_ID = "MISMATCH_FOLDER_ID";
    public static final String MISMATCH_FOLDER_ID_VALUE = "40040";
    public static final String INVALID_SNS_KIND = "INVALID_SNS_KIND";
    public static final String INVALID_SNS_KIND_VALUE = "40050";
    public static final String MISMATCH_SHARE_ID = "MISMATCH_SHARE_ID";
    public static final String MISMATCH_SHARE_ID_VALUE = "40060";

    /* 401 UNAUTHORIZED : 허락되지 않은 사용자 */
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String INVALID_TOKEN_VALUE = "40110";
    public static final String SIGNATURE = "SIGNATURE";
    public static final String SIGNATURE_VALUE = "40120";
    public static final String EXPIRED_REFRESH_TOKEN = "EXPIRED_REFRESH_TOKEN";
    public static final String EXPIRED_REFRESH_TOKEN_VALUE = "40130";
    public static final String INVALID_SNS_TOKEN = "INVALID_SNS_TOKEN";
    public static final String INVALID_SNS_TOKEN_VALUE = "40140";

    /* 403 FORBIDDEN : 잘못된 접근 */
    public static final String NOT_PERMISSION = "NOT_PERMISSION";
    public static final String NOT_PERMISSION_VALUE = "40310";
    public static final String NOT_DESERVE_ACCESS_FOLDER = "NOT_DESERVE_ACCESS_FOLDER";
    public static final String NOT_DESERVE_ACCESS_FOLDER_VALUE = "40311";
    public static final String NOT_DESERVE_POST_COMMENT = "NOT_DESERVE_POST_COMMENT";
    public static final String NOT_DESERVE_POST_COMMENT_VALUE = "40312";
    public static final String NOT_DESERVE_ACCEPT_INVITATION  = "NOT_DESERVE_ACCEPT_INVITATION";
    public static final String NOT_DESERVE_ACCEPT_INVITATION_VALUE = "40313";
    public static final String NOT_DESERVE_ACCESS_NOTIFICATION = "NOT_DESERVE_ACCESS_NOTIFICATION";
    public static final String NOT_DESERVE_ACCESS_NOTIFICATION_VALUE = "40314";
    public static final String MISMATCH_INQUIRY_OWNER = "MISMATCH_INQUIRY_OWNER";
    public static final String MISMATCH_INQUIRY_OWNER_VALUE = "40320";
    public static final String MISMATCH_FOLDER_OWNER = "MISMATCH_FOLDER_OWNER";
    public static final String MISMATCH_FOLDER_OWNER_VALUE = "40321";
    public static final String MISMATCH_COMMENT_OWNER = "MISMATCH_COMMENT_OWNER";
    public static final String MISMATCH_COMMENT_OWNER_VALUE = "40322";
    public static final String MISMATCH_ANALYSIS_OWNER = "MISMATCH_ANALYSIS_OWNER";
    public static final String MISMATCH_ANALYSIS_OWNER_VALUE = "40323";
    public static final String MISMATCH_QUIZ_RECORD_ID = "MISMATCH_QUIZ_RECORD_ID";
    public static final String MISMATCH_QUIZ_RECORD_ID_VALUE = "40324";
    public static final String MISMATCH_RECORD_OWNER = "MISMATCH_RECORD_OWNER";
    public static final String MISMATCH_RECORD_OWNER_VALUE = "40325";
    public static final String MISMATCH_RECORD_FOLDER = "MISMATCH_RECORD_FOLDER";
    public static final String MISMATCH_RECORD_FOLDER_VALUE = "40326";
    public static final String DELETED_USER = "DELETED_USER";
    public static final String DELETED_USER_VALUE = "40330";

    /* 404 NOT_FOUND : 값이 없음 */
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String NOT_FOUND_VALUE = "404";
    public static final String NOT_FOUND_USER = "NOT_FOUND_USER";
    public static final String NOT_FOUND_USER_VALUE = "40410";
    public static final String NOT_FOUND_TARGET_USER = "NOT_FOUND_TARGET_USER";
    public static final String NOT_FOUND_TARGET_USER_VALUE = "40411";
    public static final String NOT_FOUND_INQUIRY = "NOT_FOUND_INQUIRY";
    public static final String NOT_FOUND_INQUIRY_VALUE = "40420";
    public static final String NOT_FOUND_NOTICE = "NOT_FOUND_NOTICE";
    public static final String NOT_FOUND_NOTICE_VALUE = "40430";
    public static final String NOT_FOUND_NOTIFICATION = "NOT_FOUND_NOTIFICATION";
    public static final String NOT_FOUND_NOTIFICATION_VALUE = "40431";
    public static final String NOT_FOUND_NOTIFICATION_TOKEN = "NOT_FOUND_NOTIFICATION_TOKEN";
    public static final String NOT_FOUND_NOTIFICATION_TOKEN_VALUE = "40432";
    public static final String NOT_FOUND_ANSWER = "NOT_FOUND_ANSWER";
    public static final String NOT_FOUND_ANSWER_VALUE = "40440";
    public static final String NOT_FOUND_FOLDER = "NOT_FOUND_FOLDER";
    public static final String NOT_FOUND_FOLDER_VALUE = "40450";

    public static final String NOT_FOUND_TARGET_FOLDER = "NOT_FOUND_TARGET_FOLDER";
    public static final String NOT_FOUND_TARGET_FOLDER_VALUE = "40453";
    public static final String NOT_FOUND_SHARE = "NOT_FOUND_SHARE";
    public static final String NOT_FOUND_SHARE_VALUE = "40454";
    public static final String NOT_FOUND_HIGHLIGHT = "NOT_FOUND_HIGHLIGHT";
    public static final String NOT_FOUND_HIGHLIGHT_VALUE = "40460";
    public static final String NOT_FOUND_PARENT_COMMENT = "NOT_FOUND_PARENT_COMMENT";
    public static final String NOT_FOUND_PARENT_COMMENT_VALUE = "40470";
    public static final String NOT_FOUND_RECORD = "NOT_FOUND_RECORD";
    public static final String NOT_FOUND_RECORD_VALUE = "40480";
    public static final String NOT_FOUND_QUIZ = "NOT_FOUND_QUIZ";
    public static final String NOT_FOUND_QUIZ_VALUE = "40481";
    public static final String NOT_FOUND_RECORD_FIREBASE = "NOT_FOUND_RECORD_FIREBASE";
    public static final String NOT_FOUND_RECORD_FIREBASE_VALUE = "40482";
    public static final String NOT_FOUND_SECTION = "NOT_FOUND_SECTION";
    public static final String NOT_FOUND_SECTION_VALUE = "40483";
    public static final String NOT_FOUND_COMMENT = "NOT_FOUND_COMMENT";
    public static final String NOT_FOUND_COMMENT_VALUE = "40490";

    /* 409 CONFLICT : 충돌 */
    public static final String HIGHLIGHT_DUPLICATED = "HIGHLIGHT_DUPLICATED";
    public static final String HIGHLIGHT_DUPLICATED_VALUE = "40910";
    public static final String INQUIRY_ANSWER_DUPLICATED = "INQUIRY_ANSWER_DUPLICATED";
    public static final String INQUIRY_ANSWER_DUPLICATED_VALUE = "40930";
    public static final String SHARE_USER_DUPLICATED = "SHARE_USER_DUPLICATED";
    public static final String SHARE_USER_DUPLICATED_VALUE = "40940";

    /* 500 SERVER ERROR : 서버 단 에러 */
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    public static final String INTERNAL_SERVER_ERROR_VALUE = "500";
    public static final String FAILED_FILE_UPLOAD = "FAILED_FILE_UPLOAD";
    public static final String FAILED_FILE_UPLOAD_VALUE = "50010";
    public static final String REDIS_TIMEOUT = "REDIS_TIMEOUT";
    public static final String REDIS_TIMEOUT_VALUE = "50020";
    public static final String FAILED_FOLDER_CREATE = "FAILED_FOLDER_CREATE";
    public static final String FAILED_FOLDER_CREATE_VALUE = "50030";
    public static final String FAILED_EXCEL = "FAILED_EXCEL";
    public static final String FAILED_EXCEL_VALUE = "50040";
    public static final String NOT_FOUND_KEYWORD_EXCEL = "NOT_FOUND_KEYWORD_EXCEL";
    public static final String NOT_FOUND_KEYWORD_EXCEL_VALUE = "50050";
    public static final String NOT_FOUND_FOLDER_ROLE = "50060";
    public static final String NOT_FOUND_FOLDER_ROLE_VALUE = "50050";
    public static final String NOT_FOUND_USER_ROLE = "NOT_FOUND_USER_ROLE";
    public static final String NOT_FOUND_USER_ROLE_VALUE = "50051";
}