package com.koboolean.metagen.redis.service;

import com.koboolean.metagen.redis.domain.dto.ChatMessage;

public interface MessageSaveService {

    void save(ChatMessage chatMessageDto);
}
