package com.koboolean.metagen.security.exception;

import com.koboolean.metagen.security.exception.domain.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    @Getter
    ErrorCode errorCode;
    private final String customMessage;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    public CustomException(ErrorCode errorCode, String customMessage) {
        super(errorCode.getMessage() + " - " + customMessage);
        this.errorCode = errorCode;
        this.customMessage = errorCode.getMessage() + " \n[" + customMessage + "]";
    }

    public String getCustomMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }
}
