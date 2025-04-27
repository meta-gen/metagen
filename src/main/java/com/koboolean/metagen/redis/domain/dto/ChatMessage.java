package com.koboolean.metagen.redis.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private String from;
    private String to;
    private String content;
    private LocalDateTime timestamp;
}
