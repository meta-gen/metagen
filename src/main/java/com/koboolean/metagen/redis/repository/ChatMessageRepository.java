package com.koboolean.metagen.redis.repository;

import com.koboolean.metagen.redis.domain.entity.ChatMessageEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    @Query("SELECT m FROM ChatMessageEntity m WHERE " +
            "(m.sender = :userA AND m.receiver = :userB) OR " +
            "(m.sender = :userB AND m.receiver = :userA) " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessageEntity> findBySenderAndReceiver(@Param("userA") String userA, @Param("userB") String userB);

}
