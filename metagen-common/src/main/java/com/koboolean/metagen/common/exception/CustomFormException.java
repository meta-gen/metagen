package com.koboolean.metagen.common.exception;

import com.koboolean.metagen.common.exception.domain.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomFormException extends RuntimeException {
    ErrorCode errorCode;
}
