package com.koboolean.metagen.security.exception;

import com.koboolean.metagen.security.exception.domain.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(annotations = {RestController.class}, basePackages = {"com.example.yourproject.controller"})
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse response = new ErrorResponse(
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage()
        );
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    // 에러 응답을 위한 DTO 클래스
    public static class ErrorResponse {
        private final int status;
        private final String code;
        private final String message;

        public ErrorResponse(int status, String code, String message) {
            this.status = status;
            this.code = code;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
