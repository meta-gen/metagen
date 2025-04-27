package com.koboolean.metagen.redis.service.impl;

import com.koboolean.metagen.redis.domain.entity.ChatMessage;
import com.koboolean.metagen.redis.domain.entity.ChatMessageRead;
import com.koboolean.metagen.redis.repository.ChatMessageReadRepository;
import com.koboolean.metagen.redis.repository.ChatMessageRepository;
import com.koboolean.metagen.redis.service.MessageSaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageSaveServiceImpl implements MessageSaveService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageReadRepository chatMessageReadRepository;

    @Override
    @Transactional
    public void save(com.koboolean.metagen.redis.domain.dto.ChatMessage chatMessageDto) {
        ChatMessage entity = ChatMessage.builder()
                .sender(chatMessageDto.getFrom())
                .receiver(chatMessageDto.getTo())
                .content(chatMessageDto.getContent())
                .timestamp(chatMessageDto.getTimestamp() != null ? chatMessageDto.getTimestamp() : LocalDateTime.now())
                .isRemove(false)
                .build();

        chatMessageRepository.save(entity);

        ChatMessageRead chatMessageRead = ChatMessageRead.builder()
                .sender(chatMessageDto.getFrom())
                .receiver(chatMessageDto.getTo())
                .chatMessage(entity)
                .build();

        chatMessageReadRepository.save(chatMessageRead);

    }

    @Override
    @Transactional
    public List<ChatMessage> findBySenderAndReceiver(String userA, String userB) {
        return chatMessageRepository.findBySenderAndReceiver(userA, userB);
    }

    @Override
    @Transactional
    public void deleteSenderAndReceiver(String sender, String id) {
        chatMessageReadRepository.findAllBySenderAndReceiver(sender, id).forEach(chatMessageReadRepository::delete);

    }

    @Override
    public String findByBadge(String id, String id1) {
        return chatMessageReadRepository.findAllBySenderAndReceiver(id, id1).isEmpty() ? "none" : "inline";
    }
}
