package com.koboolean.metagen.chat.service;

import com.koboolean.metagen.chat.domain.dto.ChatMessage;

import java.util.List;

public interface MessageSaveService {

    void save(ChatMessage chatMessageDto);

    List<com.koboolean.metagen.chat.domain.entity.ChatMessage> findBySenderAndReceiver(String userA, String userB);

    void deleteSenderAndReceiver(String sender, String id);

    String findByBadge(String id, String id1);
}
