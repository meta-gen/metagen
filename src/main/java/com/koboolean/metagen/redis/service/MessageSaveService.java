package com.koboolean.metagen.redis.service;

import com.koboolean.metagen.redis.domain.dto.ChatMessage;

import java.util.List;

public interface MessageSaveService {

    void save(ChatMessage chatMessageDto);

    List<com.koboolean.metagen.redis.domain.entity.ChatMessage> findBySenderAndReceiver(String userA, String userB);

    void deleteSenderAndReceiver(String sender, String id);

    String findByBadge(String id, String id1);
}
