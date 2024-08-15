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
    REQUIRED_REQUEST_TOKEN(HttpStatus.BAD_REQUEST, "Must Be Requested To Request Token ! ", 40014),
    REQUIRED_USER_CODE(HttpStatus.BAD_REQUEST, "Must Be Requested To User Code ! ", 40015),
    REQUIRED_USER_ID(HttpStatus.BAD_REQUEST, "Must Be Requested To User Id ! ", 40016),

    FOLDER_DELETE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Default Folder Cannot Be Deleted ", 40020),

    DELETED_USER(HttpStatus.BAD_REQUEST, "User Deleted ! ", 40030),

    REQUIRED_FOLDER_TITLE(HttpStatus.BAD_REQUEST, "Must Be Requested To Folder Title ", 40040),
    REQUIRED_FOLDER_ID(HttpStatus.BAD_REQUEST, "Must Be Requested To Folder Id ", 40041),
    REQUIRED_QUIZ_ID(HttpStatus.BAD_REQUEST, "Must Be Requested To Quiz Id ", 40042),
    REQUIRED_RECORD_ID(HttpStatus.BAD_REQUEST, "Must Be Requested To Record Id ", 40043),
    REQUIRED_QUIZ(HttpStatus.BAD_REQUEST, "Must Be Requested To Quiz ", 40044),
    RECORD_POST_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Record Value Not Allowed Empty ! ", 40045),
    REQUIRED_RECORD_TITLE(HttpStatus.BAD_REQUEST, "Must Be Requested To Record Title ", 40046),
    REQUIRED_MOVE_ARRIVED_ID(HttpStatus.BAD_REQUEST, "Must Be Requested To Target Id ", 40047),
    INVITE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "You can't invite yourself ", 40048),
    INVITE_ACCEPT_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Already accept invitation ", 40049),

    REQUIRED_NOTICE_ID(HttpStatus.BAD_REQUEST, "Must Be Requested To Notice Id ", 40050),
    NOFI_POST_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Notification Value Not Allowed Empty ! ", 40051),
    SURVEY_POST_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Survey Value Not Allowed Empty ! ", 40052),

    NOT_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "Parent id exists in the requested comment id ", 40060),

    REQUIRED_IMAGE(HttpStatus.BAD_REQUEST, "Must Be Requested To Image ", 40070),

    REQUIRED_ROLE(HttpStatus.BAD_REQUEST, "Must Be Requested To Role Field ", 40080),
    ROLE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Role field must be only 'r' or 'w' ", 40081),

    REQUIRED_SNS_KIND(HttpStatus.BAD_REQUEST, "Must Be Requested To SnsKind Field ", 40090),
    REQUIRED_SNS_ID(HttpStatus.BAD_REQUEST, "Must Be Requested To SnsId Field ", 40091),
    REQUIRED_NAME(HttpStatus.BAD_REQUEST, "Must Be Requested To Name Field ", 40092),
    SNS_KIND_BAD_REQUEST(HttpStatus.BAD_REQUEST, "SnsKind only ' 1001 ~ 1004 ' ", 40093),
    NAME_BAD_REQUEST(HttpStatus.BAD_REQUEST, "name too long ! ", 40094),
    MISMATCH_FOLDER_ID(HttpStatus.BAD_REQUEST, "요청한 Folder Id와 DB의 정보가 일치하지 않습니다.", 40095),

    /* 401 UNAUTHORIZED : 허락되지 않은 사용자 */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Access Token Invalid", 40110),
    SIGNATURE(HttpStatus.UNAUTHORIZED, "Not Matched Token", 40120),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,  "Refresh Token Expired ! ", 40130),


    /* 403 FORBIDDEN : 잘못된 접근 */
    NOT_NULL_ROLE(HttpStatus.FORBIDDEN,"Not Allowed Null",40310),
    NOT_DESERVE_ADD_NOTICE(HttpStatus.FORBIDDEN,"Only admin or editor can write answers",40320),
    NOT_DESERVE_ACCESS_FOLDER(HttpStatus.FORBIDDEN,"Not Deserves for this folder",40321),
    NOT_DESERVE_MODIFY_INVITATION(HttpStatus.FORBIDDEN,"Can't change someone's invitation",40322),
    NOT_DESERVE_POST_COMMENT(HttpStatus.FORBIDDEN,"You aren't authorized to post comments",40323),
    NOT_DESERVE_FCM(HttpStatus.FORBIDDEN,"관리자 또는 편집자만 요청할 수 있습니다.",40324),
    NOT_DESERVE_DICTIONARY(HttpStatus.FORBIDDEN,"관리자 또는 편집자만 요청할 수 있습니다.",40325),
    INVALID_TOKEN_USER(HttpStatus.FORBIDDEN, "Invalid Token User !", 40330),
    MISMATCH_INQUIRY_OWNER(HttpStatus.FORBIDDEN, "This Inquiry isn't Your Own", 40340),
    MISMATCH_FOLDER_OWNER(HttpStatus.FORBIDDEN, "This Folder isn't Your Own", 40341),
    MISMATCH_COMMENT_OWNER(HttpStatus.FORBIDDEN, "This Comment isn't Your Own", 40342),
    MISMATCH_NOFI_OWNER(HttpStatus.FORBIDDEN, "Not Matched Owner !", 40343),
    MISMATCH_ANALYSIS_OWNER(HttpStatus.FORBIDDEN, "Not Matched Analysis Owner !", 40344),
    MISMATCH_RENAME_OWNER(HttpStatus.FORBIDDEN, "Not Matched Name Owner !", 40345),
    MISMATCH_QUIZ_RECORD_ID(HttpStatus.FORBIDDEN, "Not Matched Record Id ( Quiz not included this record ) !", 40346),
    MISMATCH_RECORD_OWNER(HttpStatus.FORBIDDEN, "Not Matched Record Owner !", 40347),
    MISMATCH_RECORD_FOLDER(HttpStatus.FORBIDDEN, "Not Matched Record Included Folder !", 40348),


    /* 404 NOT_FOUND : 값이 없음 */
    NOT_FOUND(HttpStatus.NOT_FOUND, " NOT FOUND ", 404),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, " Not Found User ! ", 40410),
    NOT_FOUND_DEFAULT_FOLDER(HttpStatus.NOT_FOUND, " Not Found Default Folder ! ", 40411),
    NOT_FOUND_TARGET_USER(HttpStatus.NOT_FOUND, " Not Found Target User ! ", 40412),
    NOT_FOUND_INQUIRY(HttpStatus.NOT_FOUND, " Not Found Inquiry ! ", 40420),
    NOT_FOUND_NOTICE(HttpStatus.NOT_FOUND, " Not Found Notice ! ", 40430),
    NOT_FOUND_ANSWER(HttpStatus.NOT_FOUND, " Not Found ANSWER ! ", 40440),
    NOT_FOUND_FOLDER(HttpStatus.NOT_FOUND, " Not Found Folder ! ", 40450),
    NOT_FOUND_ACCESSIBLE_FOLDER(HttpStatus.NOT_FOUND, " Not Found Accessible Folder ! ", 40451),
    NOT_FOUND_ORIGIN_FOLDER(HttpStatus.NOT_FOUND, " Not Found Origin Folder ! ", 40452),
    NOT_FOUND_TARGET_FOLDER(HttpStatus.NOT_FOUND, " Not Found Target Folder ! ", 40453),
    NOT_FOUND_SHARE(HttpStatus.NOT_FOUND, " Not Found Folder Share ! ", 40454),
    NOT_FOUND_FOLDER_FIREBASE(HttpStatus.NOT_FOUND, " Not Found Folder in Firebase ! ", 40455),
    NOT_FOUND_HIGHLIGHT(HttpStatus.NOT_FOUND, " Not Found Highlight ! ", 40460),
    NOT_FOUND_PARENT_COMMENT(HttpStatus.NOT_FOUND, " Not Found Parent Comment ! ", 40470),
    NOT_FOUND_RECORD(HttpStatus.NOT_FOUND, " Not Found Record ! ", 40480),
    NOT_FOUND_ANALYSIS(HttpStatus.NOT_FOUND, " Not Found Analysis ! ", 40481),
    NOT_FOUND_QUIZ(HttpStatus.NOT_FOUND, " Not Found Quiz ! ", 40482),
    NOT_FOUND_RECORD_FIREBASE(HttpStatus.NOT_FOUND, " Not Found Record in Firebase ! ", 40483),
    NOT_FOUND_SECTION(HttpStatus.NOT_FOUND, " Not Found SECTION ! ", 40484),
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, " Not Found Comment ! ", 40490),


    /* 409 CONFLICT : 충돌 */
    HIGHLIGHT_DUPLICATED(HttpStatus.CONFLICT, "Comment Highlight Already Exist ! ", 40910),
    FOLDER_NAME_DUPLICATED(HttpStatus.CONFLICT, "Folder Name Duplicated ! ", 40920),
    INQUIRY_ANSWER_DUPLICATED(HttpStatus.CONFLICT, "Answer Duplicated ! ", 40930),
    SHARE_USER_DUPLICATED(HttpStatus.CONFLICT, "This user has already been shared or sent a share request ", 40940),

    /* 500 SERVER ERROR : 서버 단 에러 */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error ! ", 500),
    FAILED_FILE_UPLOAD(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to file upload to Firebase ! ",50010),
    FAILED_FCM_SEND(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send FCM message !", 50020),
    REDIS_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS TIMEOUT ! ", 50030),
    FAILED_FOLDER_CREATE(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating folder in Firebase ! ",50040),
    FAILED_FOLDER_DELETE(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting folder in Firebase ! ",50041),
    FAILED_FIREBASE(HttpStatus.INTERNAL_SERVER_ERROR, "Firebase communication error ! ",50050),
    FAILED_EXCEL(HttpStatus.INTERNAL_SERVER_ERROR, "엑셀 변환 과정에서 오류가 발생하였습니다.",50060),
    NOT_FOUND_KEYWORD_EXCEL(HttpStatus.INTERNAL_SERVER_ERROR, "키워드 엑셀 파일을 찾을 수 없습니다.",50070),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final int errorCode;
}
