package com.koboolean.metagen.redis.controller;

import com.koboolean.metagen.redis.domain.dto.ChatMessage;
import com.koboolean.metagen.redis.service.RedisPublisher;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final static Logger log = org.slf4j.LoggerFactory.getLogger(ChatController.class);
    private final RedisPublisher redisPublisher;

    @MessageMapping("/chat.send") // /pub/chat.send
    public void sendMessage(ChatMessage message) {
        redisPublisher.publishToChannel(message.getFrom(), message.getTo(), message);
    }
}
