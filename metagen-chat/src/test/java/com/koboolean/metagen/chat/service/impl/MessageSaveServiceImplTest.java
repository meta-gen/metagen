package com.koboolean.metagen.chat.service.impl;

import com.koboolean.metagen.chat.domain.entity.ChatMessage;
import com.koboolean.metagen.chat.domain.entity.ChatMessageRead;
import com.koboolean.metagen.chat.repository.ChatMessageReadRepository;
import com.koboolean.metagen.chat.repository.ChatMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageSaveServiceImplTest {

    @Mock
    ChatMessageRepository chatMessageRepository;
    @Mock
    ChatMessageReadRepository chatMessageReadRepository;

    @InjectMocks
    MessageSaveServiceImpl service;

    private static com.koboolean.metagen.chat.domain.dto.ChatMessage message(LocalDateTime timestamp) {
        com.koboolean.metagen.chat.domain.dto.ChatMessage dto = new com.koboolean.metagen.chat.domain.dto.ChatMessage();
        dto.setFrom("1");
        dto.setTo("2");
        dto.setContent("안녕하세요");
        dto.setTimestamp(timestamp);
        return dto;
    }

    @Test
    @DisplayName("메시지를 저장하고 수신자의 안읽음 표시(ChatMessageRead)를 함께 만든다")
    void save() {
        LocalDateTime sentAt = LocalDateTime.of(2026, 1, 1, 9, 0);

        service.save(message(sentAt));

        ArgumentCaptor<ChatMessage> saved = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(saved.capture());
        assertThat(saved.getValue().getSender()).isEqualTo("1");
        assertThat(saved.getValue().getReceiver()).isEqualTo("2");
        assertThat(saved.getValue().getContent()).isEqualTo("안녕하세요");
        assertThat(saved.getValue().getTimestamp()).isEqualTo(sentAt);
        assertThat(saved.getValue().getIsRemove()).isFalse();

        ArgumentCaptor<ChatMessageRead> unread = ArgumentCaptor.forClass(ChatMessageRead.class);
        verify(chatMessageReadRepository).save(unread.capture());
        assertThat(unread.getValue().getSender()).isEqualTo("1");
        assertThat(unread.getValue().getReceiver()).isEqualTo("2");
        assertThat(unread.getValue().getChatMessage()).isSameAs(saved.getValue());
    }

    @Test
    @DisplayName("전송 시각이 없으면 서버 시각으로 저장한다")
    void defaultTimestamp() {
        LocalDateTime before = LocalDateTime.now();

        service.save(message(null));

        ArgumentCaptor<ChatMessage> saved = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(saved.capture());
        assertThat(saved.getValue().getTimestamp()).isAfterOrEqualTo(before);
    }

    @Test
    @DisplayName("안읽은 메시지가 있으면 뱃지 'inline', 없으면 'none'")
    void badge() {
        when(chatMessageReadRepository.findAllBySenderAndReceiver("1", "2")).thenReturn(List.of(new ChatMessageRead()));
        when(chatMessageReadRepository.findAllBySenderAndReceiver("3", "2")).thenReturn(List.of());

        assertThat(service.findByBadge("1", "2")).isEqualTo("inline");
        assertThat(service.findByBadge("3", "2")).isEqualTo("none");
    }

    @Test
    @DisplayName("대화를 열면(읽음 처리) 해당 발신자 → 나 의 안읽음 표시를 모두 삭제한다")
    void markRead() {
        ChatMessageRead first = new ChatMessageRead();
        ChatMessageRead second = new ChatMessageRead();
        when(chatMessageReadRepository.findAllBySenderAndReceiver("1", "2")).thenReturn(List.of(first, second));

        service.deleteSenderAndReceiver("1", "2");

        verify(chatMessageReadRepository).delete(first);
        verify(chatMessageReadRepository).delete(second);
    }
}
