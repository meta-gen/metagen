package com.koboolean.metagen.redis.service.impl;

import com.koboolean.metagen.redis.domain.dto.ChatMessage;
import com.koboolean.metagen.redis.domain.entity.ChatMessageEntity;
import com.koboolean.metagen.redis.repository.ChatMessageRepository;
import com.koboolean.metagen.redis.service.MessageSaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageSaveServiceImpl implements MessageSaveService {

    private final ChatMessageRepository chatMessageRepository;

    @Override
    @Transactional
    public void save(ChatMessage chatMessageDto) {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .sender(chatMessageDto.getFrom())
                .receiver(chatMessageDto.getTo())
                .content(chatMessageDto.getContent())
                .timestamp(chatMessageDto.getTimestamp() != null ? chatMessageDto.getTimestamp() : LocalDateTime.now())
                .build();

        chatMessageRepository.save(entity);
    }
}
