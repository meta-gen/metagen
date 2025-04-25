package com.koboolean.metagen.redis.service;

import com.koboolean.metagen.redis.domain.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publishToChannel(String from, String to, ChatMessage message) {
        String channel = Stream.of(from, to).sorted().collect(Collectors.joining("-"));

        System.out.println("publishToChannel = " + channel);
        redisTemplate.convertAndSend(channel, message);
    }
}
