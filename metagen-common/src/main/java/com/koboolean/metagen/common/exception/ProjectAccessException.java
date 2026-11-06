package com.koboolean.metagen.common.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 로그인 시 선택한 프로젝트에 접근할 수 없는 경우
 */
public class ProjectAccessException extends AuthenticationException {
    public ProjectAccessException(String message) {
        super(message);
    }
}
