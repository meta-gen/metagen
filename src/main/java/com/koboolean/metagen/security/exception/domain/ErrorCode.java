package com.koboolean.metagen.security.exception.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /**
     * 알수없는 오류가 발생하였습니다.
     */
    UNKNOWN_EXCEPTION(HttpStatus.BAD_REQUEST, "UNKNOWN-EXCEPTION", "알수없는 오류가 발생하였습니다."),

    /**
     * 패스워드가 일치하지 않습니다.
     */
    PASSWORD_NOT_MATCHED(HttpStatus.NOT_FOUND, "PASSWORD-NOT-MATCHED", "패스워드가 일치하지 않습니다."),


    /**
     * 엑셀 생성이 실패하였습니다.
     */
    CREATE_EXCEL_FAIL_EXCEPTION(HttpStatus.NOT_FOUND, "CREATE_EXCEL_FAIL_EXCEPTION", "엑셀 생성이 실패하였습니다."),

    /**
     * 등록된 아이디가 존재합니다.
     */
    USERNAME_IS_DUPLICATION(HttpStatus.BAD_REQUEST, "USERNAME_IS_DUPLICATION", "등록된 아이디가 존재합니다."),

    /**
     * 비밀번호는 8자 이상, 대문자 및 특수문자를 각각 하나 이상 포함해야 합니다.
     */
    PASSWORD_IS_NON_VALIDATOR(HttpStatus.BAD_REQUEST, "PASSWORD_IS_NON_VALIDATOR", "비밀번호는 8자 이상, 대문자 및 특수문자를 각각 하나 이상 포함해야 합니다."),

    /**
     * 아이디는 5~20자의 소문자와 숫자만 사용할 수 있습니다.
     */
    USERNAME_IS_NON_VALIDATOR(HttpStatus.BAD_REQUEST, "USERNAME_IS_NON_VALIDATOR", "아이디는 5~20자의 소문자와 숫자만 사용할 수 있습니다."),

    /**
     * 동일한 형식의 표준 용어와 표준 도메인이 존재합니다. 중복된 용어는 등록이 불가능합니다.
     */
    DUPLICATE_TERM_DATA(HttpStatus.BAD_REQUEST, "DUPLICATE_TERM_DATA", "동일한 형식의 표준 용어와 표준 도메인이 존재합니다. 중복된 용어는 등록이 불가능합니다."),


    /**
     * 해당 용어를 찾을 수 없습니다.
     */
    NOT_FOUND_TERM_DATA(HttpStatus.BAD_REQUEST, "NOT_FOUND_TERM_DATA", "해당 용어를 찾을 수 없습니다."),

    /**
     * 등록 용어중 매핑되지 않는 단어가 존재합니다.
     */
    NOT_FOUND_WORD_DATA(HttpStatus.BAD_REQUEST, "NOT_FOUND_WORD_DATA", "등록 용어중 매핑되지 않는 단어가 존재합니다."),

    /**
     * 승인된 정보는 삭제할 수 없습니다.
     */
    APPROVED_DATA_CANNOT_BE_DELETED(HttpStatus.BAD_REQUEST, "APPROVED_DATA_CANNOT_BE_DELETED", "승인된 정보는 삭제할 수 없습니다."),

    /**
     * 사용 중인 정보는 삭제할 수 없습니다.
     */
    RELATION_EXISTS(HttpStatus.BAD_REQUEST, "RELATION_EXISTS", "사용 중인 정보는 삭제할 수 없습니다."),

    /**
     * 해당 도메인을 찾을 수 없습니다.
     */
    NOT_FOUND_DOMAIN_DATA(HttpStatus.BAD_REQUEST, "NOT_FOUND_DOMAIN_DATA", "해당 도메인을 찾을 수 없습니다."),

    /**
     * 권한이 없어 작업을 수행할 수 없습니다.
     */
    DATA_CANNOT_BE_DELETED(HttpStatus.BAD_REQUEST, "DATA_CANNOT_BE_DELETED", "권한이 없어 작업을 수행할 수 없습니다."),

    /**
     * 프로젝트 관리자는 삭제가 불가능합니다.
     */
    MANAGER_NON_DELETED(HttpStatus.BAD_REQUEST, "MANAGER_NON_DELETED", "관리자는 변경이 불가능합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
