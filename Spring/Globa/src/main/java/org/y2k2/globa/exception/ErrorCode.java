package org.y2k2.globa.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /* 400 BAD_REQUEST : 잘못된 요청 */
    EXPIRED_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "Access 토큰이 만료되었습니다. ", 40010),
    ACTIVE_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "Refresh 토큰이 만료되지 않았습니다.", 40011),
    NOT_MATCH_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "Refresh 토큰이 일치하지 않습니다. ", 40012),
    REQUIRED_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "인자로 Access 토큰이 필요합니다. ", 40013),
    REQUIRED_REQUEST_TOKEN(HttpStatus.BAD_REQUEST, "인자로 Refresh 토큰이 필요합니다. ", 40014),
    REQUIRED_USER_CODE(HttpStatus.BAD_REQUEST, "인자로 User Code가 필요합니다. ", 40015),
    REQUIRED_USER_ID(HttpStatus.BAD_REQUEST, "인자로 User Id가 필요합니다. ", 40016),
    REQUIRED_SNS_TOKEN(HttpStatus.BAD_REQUEST, "인자로 SNS 토큰이 필요합니다. ", 40017),

    FOLDER_DELETE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "기본 폴더는 삭제할 수 없습니다. ", 40020),

    DELETED_USER(HttpStatus.BAD_REQUEST, "탈퇴된 유저입니다. ", 40030),

    REQUIRED_FOLDER_TITLE(HttpStatus.BAD_REQUEST, "인자로 Folder Title이 필요합니다. ", 40040),
    REQUIRED_FOLDER_ID(HttpStatus.BAD_REQUEST, "인자로 Folder Id가 필요합니다. ", 40041),
    REQUIRED_QUIZ_ID(HttpStatus.BAD_REQUEST, "인자로 Quiz Id가 필요합니다. ", 40042),
    REQUIRED_RECORD_ID(HttpStatus.BAD_REQUEST, "인자로 Record Id가 필요합니다. ", 40043),
    REQUIRED_QUIZ(HttpStatus.BAD_REQUEST, "인자로 Quiz가 필요합니다. ", 40044),
    RECORD_POST_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Record를 등록하기 위해선 빈칸이 허용되지 않습니다. ", 40045),
    REQUIRED_RECORD_TITLE(HttpStatus.BAD_REQUEST, "인자로 Record Title이 필요합니다. ", 40046),
    REQUIRED_MOVE_ARRIVED_ID(HttpStatus.BAD_REQUEST, "인자로 Target Id가 필요합니다. ", 40047),
    INVITE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "자기자신을 초대할 수 없습니다. ", 40048),
    INVITE_ACCEPT_BAD_REQUEST(HttpStatus.BAD_REQUEST, "이미 수락된 초대입니다. ", 40049),

    REQUIRED_NOTICE_ID(HttpStatus.BAD_REQUEST, "인자로 Notice Id가 필요합니다. ", 40050),
    NOFI_POST_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Notification을 등록하기 위해선 빈칸이 허용되지 않습니다. ", 40051),
    SURVEY_POST_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Survey를 등록하기 위해선 빈칸이 허용되지 않습니다. ", 40052),
    NOFI_TYPE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Notification의 Type은 'a','n','s',r' 의 값만 가질 수 있습니다. ", 40053),
    REQUIRED_NOTIFICATION_ID(HttpStatus.BAD_REQUEST, "인자로 Notification Id가 필요합니다. ", 40054),

    NOT_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "요청된 댓글에 부모 댓글이 존재합니다. ", 40060),

    REQUIRED_IMAGE(HttpStatus.BAD_REQUEST, "인자로 Image가 필요합니다. ", 40070),

    REQUIRED_ROLE(HttpStatus.BAD_REQUEST, "인자로 Rolde Field가 필요합니다. ", 40080),
    ROLE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Role Field는 'r' 혹은 'w'의 값만 가질 수 있습니다. ", 40081),

    REQUIRED_SNS_KIND(HttpStatus.BAD_REQUEST, "인자로 SnsKind가 필요합니다. ", 40090),
    REQUIRED_SNS_ID(HttpStatus.BAD_REQUEST, "인자로 SnsId가 필요합니다. ", 40091),
    REQUIRED_NAME(HttpStatus.BAD_REQUEST, "인자로 Name이 필요합니다. ", 40092),
    SNS_KIND_BAD_REQUEST(HttpStatus.BAD_REQUEST, "SnsKind는 오직 ' 1001 ~ 1004 ' 사이의 값만 가질 수 있습니다. ", 40093),
    NAME_BAD_REQUEST(HttpStatus.BAD_REQUEST, "이름의 길이가 너무 깁니다. ", 40094),
    MISMATCH_FOLDER_ID(HttpStatus.BAD_REQUEST, "요청한 Folder Id가 일치하지 않습니다.", 40095),

    /* 401 UNAUTHORIZED : 허락되지 않은 사용자 */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Access 토큰입니다. ", 40110),
    SIGNATURE(HttpStatus.UNAUTHORIZED, "토큰이 일치하지 않습니다. ", 40120),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,  "Refresh 토큰이 만료되었습니다. ", 40130),
    INVALID_SNS_TOKEN(HttpStatus.UNAUTHORIZED,  "유효하지 않은 Sns Token 입니다. ", 40140),


    /* 403 FORBIDDEN : 잘못된 접근 */
    NOT_NULL_ROLE(HttpStatus.FORBIDDEN,"Role은 Null 값이 허용되지 않습니다.",40310),
    NOT_DESERVE_ADD_NOTICE(HttpStatus.FORBIDDEN,"오직 admin 혹은 editor만 작성할 수 있습니다.",40320),
    NOT_DESERVE_ACCESS_FOLDER(HttpStatus.FORBIDDEN,"해당 폴더에 대한 접근 권한이 없습니다. ",40321),
    NOT_DESERVE_MODIFY_INVITATION(HttpStatus.FORBIDDEN,"초대할 수 있는 권한이 없습니다. ",40322),
    NOT_DESERVE_POST_COMMENT(HttpStatus.FORBIDDEN,"댓글을 작성할 수 있는 권한이 없습니다. ",40323),
    NOT_DESERVE_FCM(HttpStatus.FORBIDDEN,"관리자 또는 편집자만 요청할 수 있습니다.",40324),
    NOT_DESERVE_DICTIONARY(HttpStatus.FORBIDDEN,"관리자 또는 편집자만 요청할 수 있습니다.",40325),
    INVALID_TOKEN_USER(HttpStatus.FORBIDDEN, "유효하지 않은 Token User 입니다. ", 40330),
    MISMATCH_INQUIRY_OWNER(HttpStatus.UNAUTHORIZED, "해당 문의에 대한 소유권자가 아닙니다.", 40340),
    MISMATCH_FOLDER_OWNER(HttpStatus.UNAUTHORIZED, "해당 폴더에 대한 소유권자가 아닙니다.", 40341),
    MISMATCH_COMMENT_OWNER(HttpStatus.UNAUTHORIZED, "해당 댓글에 대한 소유권자가 아닙니다.", 40342),
    MISMATCH_NOFI_OWNER(HttpStatus.UNAUTHORIZED, "해당 알림에 대한 소유권자가 아닙니다.", 40343),
    MISMATCH_ANALYSIS_OWNER(HttpStatus.UNAUTHORIZED, "해당 분석에 대한 소유권자가 아닙니다.", 40344),
    MISMATCH_RENAME_OWNER(HttpStatus.UNAUTHORIZED, "이름 변경을 위한 권한이 없습니다. ", 40345),
    MISMATCH_QUIZ_RECORD_ID(HttpStatus.UNAUTHORIZED, "해당 문서의 퀴즈가 아닙니다.", 40346),
    MISMATCH_RECORD_OWNER(HttpStatus.UNAUTHORIZED, "해당 문서에 대한 소유권자가 아닙니다.", 40347),
    MISMATCH_RECORD_FOLDER(HttpStatus.UNAUTHORIZED, "해당 폴더의 문서가 아닙니다. ", 40348),


    /* 404 NOT_FOUND : 값이 없음 */
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, " 유저를 찾을 수 없습니다. ", 40410),
    NOT_FOUND_DEFAULT_FOLDER(HttpStatus.NOT_FOUND, " 기본 폴더를 찾을 수 없습니다. ", 40411),
    NOT_FOUND_TARGET_USER(HttpStatus.NOT_FOUND, " 대상 유저를 찾을 수 없습니다. ", 40412),
    NOT_FOUND_INQUIRY(HttpStatus.NOT_FOUND, " 문의를 찾을 수 없습니다. ", 40420),
    NOT_FOUND_NOTICE(HttpStatus.NOT_FOUND, " 공지를 찾을 수 없습니다. ", 40430),
    NOT_FOUND_NOTIFICATION(HttpStatus.NOT_FOUND, " 알림을 찾을 수 없습니다. ", 40431),
    NOT_FOUND_ANSWER(HttpStatus.NOT_FOUND, " 답변을 찾을 수 없습니다. ", 40440),
    NOT_FOUND_FOLDER(HttpStatus.NOT_FOUND, " 폴더를 찾을 수 없습니다. ", 40450),
    NOT_FOUND_ACCESSIBLE_FOLDER(HttpStatus.NOT_FOUND, " 폴더에 대한 접근 권한을 찾을 수 없습니다. ", 40451),
    NOT_FOUND_ORIGIN_FOLDER(HttpStatus.NOT_FOUND, " 기존 폴더를 찾을 수 없습니다. ", 40452),
    NOT_FOUND_TARGET_FOLDER(HttpStatus.NOT_FOUND, " 대상 폴더를 찾을 수 없습니다. ", 40453),
    NOT_FOUND_SHARE(HttpStatus.NOT_FOUND, " 공유 이력을 찾을 수 없습니다. ", 40454),
    NOT_FOUND_FOLDER_FIREBASE(HttpStatus.NOT_FOUND, " 해당 폴더를 파이어베이스에서 찾을 수 없습니다. ", 40455),
    NOT_FOUND_HIGHLIGHT(HttpStatus.NOT_FOUND, " 하이라이트를 찾을 수 없습니다. ", 40460),
    NOT_FOUND_PARENT_COMMENT(HttpStatus.NOT_FOUND, " 부모 댓글을 찾을 수 없습니다. ", 40470),
    NOT_FOUND_RECORD(HttpStatus.NOT_FOUND, " 문서를 찾을 수 없습니다. ", 40480),
    NOT_FOUND_ANALYSIS(HttpStatus.NOT_FOUND, " 분석을 찾을 수 없습니다. ", 40481),
    NOT_FOUND_QUIZ(HttpStatus.NOT_FOUND, " 퀴즈를 차즐 수 없습니다. ", 40482),
    NOT_FOUND_RECORD_FIREBASE(HttpStatus.NOT_FOUND, " 해당 문서를 파이어베이스에서 찾을 수 없습니다. ", 40483),
    NOT_FOUND_SECTION(HttpStatus.NOT_FOUND, " 섹션을 찾을 수 없습니다. ", 40484),
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, " 댓글을 찾을 수 없습니다. ", 40490),


    /* 409 CONFLICT : 충돌 */
    HIGHLIGHT_DUPLICATED(HttpStatus.CONFLICT, "해당 댓글에 이미 하이라이트가 존재합니다. ", 40910),
    FOLDER_NAME_DUPLICATED(HttpStatus.CONFLICT, "폴더 이름이 이미 존재합니다. ", 40920),
    INQUIRY_ANSWER_DUPLICATED(HttpStatus.CONFLICT, "답변이 이미 존재합니다. ", 40930),
    SHARE_USER_DUPLICATED(HttpStatus.CONFLICT, "해당 유저에게는 이미 공유되었거나, 공유 요청이 전송된 상태입니다. ", 40940),
    NOTIFICATION_READ_DUPLICATED(HttpStatus.CONFLICT, "이미 읽은 알림입니다. ", 40950),

    /* 500 SERVER ERROR : 서버 단 에러 */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error ! ", 500),
    FAILED_FILE_UPLOAD(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 파이어베이스에 업로드가 실패하였습니다. ",50010),
    FAILED_FCM_SEND(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 메시지 전송에 실패하였습니다. ", 50020),
    REDIS_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS TIMEOUT ! ", 50030),
    FAILED_FOLDER_CREATE(HttpStatus.INTERNAL_SERVER_ERROR, "파이어베이스에서 폴더 생성에 오류가 실패하였습니다. ",50040),
    FAILED_FOLDER_DELETE(HttpStatus.INTERNAL_SERVER_ERROR, "파이어베이스에서 폴더 삭제에 오류가 실패하였습니다. ",50041),
    FAILED_FIREBASE(HttpStatus.INTERNAL_SERVER_ERROR, "파이어베이스와 통신에 실패하였습니다. ",50050),
    FAILED_EXCEL(HttpStatus.INTERNAL_SERVER_ERROR, "엑셀 변환 과정에서 오류가 발생하였습니다. ",50060),
    NOT_FOUND_KEYWORD_EXCEL(HttpStatus.INTERNAL_SERVER_ERROR, "키워드 엑셀 파일을 찾을 수 없습니다. ",50070),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final int errorCode;
}
