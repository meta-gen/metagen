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
    USERNAME_IS_DUPLICATION(HttpStatus.BAD_REQUEST, "USERNAME_IS_DUPLICATION", "등록된 아이디가 존재합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
