package com.koboolean.metagen.redis.config;

import com.koboolean.metagen.redis.service.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class RedisSubscriberManager {

    private final RedisMessageListenerContainer container;
    private final RedisSubscriber redisSubscriber;

    private final Set<String> subscribedChannels = ConcurrentHashMap.newKeySet(); // thread-safe set

    public void subscribeChannel(String from, String to) {
        String channel = Stream.of(from, to).sorted().collect(Collectors.joining("-"));

        if (subscribedChannels.contains(channel)) {
            return; // 이미 구독한 채널은 무시
        }

        subscribedChannels.add(channel);

        container.addMessageListener(redisSubscriber, new ChannelTopic(channel));
    }
}
