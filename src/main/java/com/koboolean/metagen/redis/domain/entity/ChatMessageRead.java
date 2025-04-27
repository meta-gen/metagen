package com.koboolean.metagen.redis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRead {

    @Id
    @GeneratedValue
    private Long id;

    private String sender;

    private String receiver;

    @OneToOne
    private ChatMessage chatMessage;

}
