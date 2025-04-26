package com.koboolean.metagen.redis.controller;

import com.koboolean.metagen.redis.config.RedisSubscriberManager;
import com.koboolean.metagen.redis.domain.entity.ChatMessage;
import com.koboolean.metagen.redis.repository.ChatMessageRepository;
import com.koboolean.metagen.redis.service.MessageSaveService;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatHistoryRestController {

    private final MessageSaveService messageSaveService;
    private final RedisSubscriberManager redisSubscriberManager;

    @GetMapping("/history")
    public List<ChatMessage> getChatHistory(@RequestParam(value = "user") String user, @AuthenticationPrincipal AccountDto accountDto) {

        String myId = accountDto.getId();

        String userA = Stream.of(myId, user).sorted().collect(Collectors.toList()).get(0);
        String userB = Stream.of(myId, user).sorted().collect(Collectors.toList()).get(1);

        redisSubscriberManager.subscribeChannel(userA, userB);

        return messageSaveService.findBySenderAndReceiver(userA, userB);
    }

    @DeleteMapping("/history/{sender}")
    public ResponseEntity<Map<String, Boolean>> deleteChatHistory(@PathVariable(value = "sender") String sender, @AuthenticationPrincipal AccountDto accountDto) {

        messageSaveService.deleteSenderAndReceiver(sender, accountDto.getId());

        return ResponseEntity.ok(Map.of("result", true));
    }
}
