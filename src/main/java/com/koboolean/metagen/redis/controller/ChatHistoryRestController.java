package com.koboolean.metagen.redis.controller;

import com.koboolean.metagen.redis.config.RedisSubscriberManager;
import com.koboolean.metagen.redis.domain.entity.ChatMessageEntity;
import com.koboolean.metagen.redis.repository.ChatMessageRepository;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatHistoryRestController {

    private final ChatMessageRepository chatMessageRepository;
    private final RedisSubscriberManager redisSubscriberManager;

    @GetMapping("/history")
    public List<ChatMessageEntity> getChatHistory(@RequestParam(value = "user") String user, @AuthenticationPrincipal AccountDto accountDto) {

        String myId = accountDto.getId();

        String userA = Stream.of(myId, user).sorted().collect(Collectors.toList()).get(0);
        String userB = Stream.of(myId, user).sorted().collect(Collectors.toList()).get(1);

        redisSubscriberManager.subscribeChannel(userA, userB);

        return chatMessageRepository.findBySenderAndReceiver(userA, userB);
    }
}
