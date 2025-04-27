package com.koboolean.metagen.redis.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String receiver;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;

    private Boolean isRemove;
}
