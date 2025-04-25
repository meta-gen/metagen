package com.koboolean.metagen.redis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koboolean.metagen.redis.domain.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RedisSubscriber implements org.springframework.data.redis.connection.MessageListener{

    private final Logger log = LoggerFactory.getLogger(RedisSubscriber.class);

    private final SimpMessagingTemplate messagingTemplate; // WebSocket으로 전달
    private final ObjectMapper objectMapper;
    private final MessageSaveService messageSaveService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            ChatMessage chat = objectMapper.readValue(body, ChatMessage.class);

            messageSaveService.save(chat);
            messagingTemplate.convertAndSend("/sub/chat/" + chat.getTo(), chat);
        } catch (Exception e) {
            log.error("Redis 수신 실패", e);
        }
    }
}
