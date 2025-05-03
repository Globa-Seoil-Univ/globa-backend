package org.y2k2.globa.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    /* 400 BAD_REQUEST : 잘못된 요청 */
    EXPIRED_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "Access 토큰이 만료되었습니다. ", "40010"),
    ACTIVE_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "Access 토큰이 만료되지 않았습니다.", "40011"),
    NOT_MATCH_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "Refresh 토큰이 일치하지 않습니다.", "40012"),
    INVITE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "자기 자신을 초대할 수 없습니다.", "40020"),
    INVITE_ACCEPT_BAD_REQUEST(HttpStatus.BAD_REQUEST, "이미 수락된 초대입니다.", "40021"),
    NOT_INCLUDE_HIGHLIGHT_COMMENT(HttpStatus.BAD_REQUEST, "해당 하이라이트 댓글이 아닙니다.", "40030"),
    MISMATCH_FOLDER_ID(HttpStatus.BAD_REQUEST, "요청한 Folder Id가 일치하지 않습니다.", "40040"),
    INVALID_SNS_KIND(HttpStatus.BAD_REQUEST, "유효하지 않은 Sns Kind 입니다.", "40050"),
    MISMATCH_SHARE_ID(HttpStatus.BAD_REQUEST, "요청한 공유 ID가 일치하지 않습니다.", "40060"),

    /* 401 UNAUTHORIZED : 허락되지 않은 사용자 */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Access 토큰입니다.", "40110"),
    SIGNATURE(HttpStatus.UNAUTHORIZED, "토큰이 일치하지 않습니다.", "40120"),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,  "Refresh 토큰이 만료되었습니다.", "40130"),
    INVALID_SNS_TOKEN(HttpStatus.UNAUTHORIZED,  "유효하지 않은 Sns Token 입니다.", "40140"),

    /* 403 FORBIDDEN : 잘못된 접근 */
    NOT_PERMISSION(HttpStatus.FORBIDDEN,"오직 관리자와 편집자만 작성할 수 있습니다.","40310"),
    NOT_DESERVE_ACCESS_FOLDER(HttpStatus.FORBIDDEN,"해당 폴더에 대한 접근 권한이 없습니다.","40311"),
    NOT_DESERVE_POST_COMMENT(HttpStatus.FORBIDDEN,"댓글을 작성할 수 있는 권한이 없습니다.","40312"),
    NOT_DESERVE_ACCEPT_INVITATION(HttpStatus.FORBIDDEN,"초대를 수락할 수 있는 권한이 없습니다.","40313"),
    NOT_DESERVE_ACCESS_NOTIFICATION(HttpStatus.FORBIDDEN,"알림에 대한 접근 권한이 없습니다. ","40314"),
    MISMATCH_INQUIRY_OWNER(HttpStatus.FORBIDDEN, "해당 문의에 대한 소유권자가 아닙니다.", "40320"),
    MISMATCH_FOLDER_OWNER(HttpStatus.FORBIDDEN, "해당 폴더에 대한 소유권자가 아닙니다.", "40321"),
    MISMATCH_COMMENT_OWNER(HttpStatus.FORBIDDEN, "해당 댓글에 대한 소유권자가 아닙니다.", "40322"),
    MISMATCH_ANALYSIS_OWNER(HttpStatus.FORBIDDEN, "해당 분석에 대한 소유권자가 아닙니다.", "40323"),
    MISMATCH_QUIZ_RECORD_ID(HttpStatus.FORBIDDEN, "해당 문서의 퀴즈가 아닙니다.", "40324"),
    MISMATCH_RECORD_OWNER(HttpStatus.FORBIDDEN, "해당 문서에 대한 소유권자가 아닙니다.", "40325"),
    MISMATCH_RECORD_FOLDER(HttpStatus.FORBIDDEN, "해당 폴더의 문서가 아닙니다.", "40326"),
    DELETED_USER(HttpStatus.FORBIDDEN, "탈퇴된 유저입니다.", "40330"),

    /* 404 NOT_FOUND : 값이 없음 */
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다.", "40410"),
    NOT_FOUND_TARGET_USER(HttpStatus.NOT_FOUND, "대상 유저를 찾을 수 없습니다.", "40411"),
    NOT_FOUND_ROLE(HttpStatus.NOT_FOUND, "권한을 찾을 수 없습니다. ", "40412"),
    NOT_FOUND_INQUIRY(HttpStatus.NOT_FOUND, "문의를 찾을 수 없습니다.", "40420"),
    NOT_FOUND_NOTICE(HttpStatus.NOT_FOUND, "공지를 찾을 수 없습니다.", "40430"),
    NOT_FOUND_NOTIFICATION(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.", "40431"),
    NOT_FOUND_NOTIFICATION_TOKEN(HttpStatus.NOT_FOUND, "알림 토큰을 찾을 수 없습니다.", "40432"),
    NOT_FOUND_ANSWER(HttpStatus.NOT_FOUND, "답변을 찾을 수 없습니다.", "40440"),
    NOT_FOUND_FOLDER(HttpStatus.NOT_FOUND, "폴더를 찾을 수 없습니다.", "40450"),
    NOT_FOUND_TARGET_FOLDER(HttpStatus.NOT_FOUND, "대상 폴더를 찾을 수 없습니다. ", "40451"),
    NOT_FOUND_SHARE(HttpStatus.NOT_FOUND, "공유 이력을 찾을 수 없습니다.", "40452"),
    NOT_FOUND_HIGHLIGHT(HttpStatus.NOT_FOUND, "하이라이트를 찾을 수 없습니다.", "40460"),
    NOT_FOUND_PARENT_COMMENT(HttpStatus.NOT_FOUND, "부모 댓글을 찾을 수 없습니다.", "40470"),
    NOT_FOUND_RECORD(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다.", "40480"),
    NOT_FOUND_QUIZ(HttpStatus.NOT_FOUND, "퀴즈를 차즐 수 없습니다.", "40481"),
    NOT_FOUND_RECORD_FIREBASE(HttpStatus.NOT_FOUND, "해당 문서를 파이어베이스에서 찾을 수 없습니다.", "40482"),
    NOT_FOUND_SECTION(HttpStatus.NOT_FOUND, "섹션을 찾을 수 없습니다.", "40483"),
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다.", "40490"),

    /* 409 CONFLICT : 충돌 */
    HIGHLIGHT_DUPLICATED(HttpStatus.CONFLICT, "해당 댓글에 이미 하이라이트가 존재합니다.", "40910"),
    INQUIRY_ANSWER_DUPLICATED(HttpStatus.CONFLICT, "답변이 이미 존재합니다.", "40920"),
    SHARE_USER_DUPLICATED(HttpStatus.CONFLICT, "해당 유저에게는 이미 공유되었거나, 공유 요청이 전송된 상태입니다.", "40930"),

    /* 500 SERVER ERROR : 서버 단 에러 */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예기치 못한 서버 에러가 발생하였습니다.", "500"),
    FAILED_FILE_UPLOAD(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 파이어베이스에 업로드가 실패하였습니다.","50010"),
    REDIS_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 연결 시간이 초과되었습니다.", "50020"),
    FAILED_FOLDER_CREATE(HttpStatus.INTERNAL_SERVER_ERROR, "파이어베이스에서 폴더 생성에 오류가 실패하였습니다.","50030"),
    FAILED_EXCEL(HttpStatus.INTERNAL_SERVER_ERROR, "엑셀 변환 과정에서 오류가 발생하였습니다.","50040"),
    NOT_FOUND_KEYWORD_EXCEL(HttpStatus.INTERNAL_SERVER_ERROR, "키워드 엑셀 파일을 찾을 수 없습니다.","50050"),
    NOT_FOUND_FOLDER_ROLE(HttpStatus.INTERNAL_SERVER_ERROR, "폴더 권한을 찾을 수 없습니다.", "50051"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;
}
