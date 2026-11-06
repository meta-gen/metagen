package com.koboolean.metagen.chat.controller;

import com.koboolean.metagen.chat.domain.dto.ChatMessage;
import com.koboolean.metagen.chat.service.MessageSaveService;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate; // WebSocket으로 전달
    private final MessageSaveService messageSaveService;

    @MessageMapping("/chat.send") // /pub/chat.send
    public void sendMessage(ChatMessage message, Authentication authentication) {
        // 보낸 사람은 클라이언트 값이 아닌 로그인 사용자로 지정 (다른 사용자 사칭 방지)
        AccountDto accountDto = (AccountDto) authentication.getPrincipal();
        message.setFrom(accountDto.getId());

        messageSaveService.save(message);

        messagingTemplate.convertAndSend("/sub/chat/" + message.getTo(), message);
    }
}
