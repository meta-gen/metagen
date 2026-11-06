package com.koboolean.metagen.chat.repository;

import com.koboolean.metagen.chat.domain.entity.ChatMessageRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageReadRepository extends JpaRepository<ChatMessageRead, Long> {
    List<ChatMessageRead> findAllBySenderAndReceiver(String sender, String receiver);
}
