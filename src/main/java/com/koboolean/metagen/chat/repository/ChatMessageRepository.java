package com.koboolean.metagen.chat.repository;

import com.koboolean.metagen.chat.domain.entity.ChatMessage;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.sender = :userA AND m.receiver = :userB) OR " +
            "(m.sender = :userB AND m.receiver = :userA) " +
            "AND (m.isRemove = false OR m.isRemove IS NULL) " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessage> findBySenderAndReceiver(@Param("userA") String userA, @Param("userB") String userB);

}
