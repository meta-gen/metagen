package com.koboolean.metagen.chat.service;

import com.koboolean.metagen.core.security.domain.dto.AccountDto;

import java.util.Map;

/**
 * 메신저 사용자 목록 (접속/미접속 + 읽지 않은 메시지 뱃지)
 */
public interface ChatUserService {

    Map<String, Object> getActiveUser(AccountDto accountDto);
}
