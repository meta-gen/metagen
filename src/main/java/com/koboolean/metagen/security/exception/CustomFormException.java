package com.koboolean.metagen.security.exception;

import com.koboolean.metagen.security.exception.domain.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomFormException extends RuntimeException {
    ErrorCode errorCode;
}
