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
    PASSWORD_NOT_MATCHED(HttpStatus.NOT_FOUND, "PASSWORD-NOT-MATCHED", "패스워드가 일치하지 않습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
