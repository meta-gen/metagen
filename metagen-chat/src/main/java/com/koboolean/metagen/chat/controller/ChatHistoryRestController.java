package com.koboolean.metagen.chat.controller;

import com.koboolean.metagen.chat.domain.entity.ChatMessage;
import com.koboolean.metagen.chat.repository.ChatMessageRepository;
import com.koboolean.metagen.chat.service.MessageSaveService;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@PreAuthorize("hasRole('NOT_APPROVE')")
public class ChatHistoryRestController {

    private final MessageSaveService messageSaveService;

    @GetMapping("/history")
    public List<ChatMessage> getChatHistory(@RequestParam(value = "user") String user, @AuthenticationPrincipal AccountDto accountDto) {

        String myId = accountDto.getId();

        String userA = Stream.of(myId, user).sorted().collect(Collectors.toList()).get(0);
        String userB = Stream.of(myId, user).sorted().collect(Collectors.toList()).get(1);

        return messageSaveService.findBySenderAndReceiver(userA, userB);
    }

    @DeleteMapping("/history/{sender}")
    public ResponseEntity<Map<String, Boolean>> deleteChatHistory(@PathVariable(value = "sender") String sender, @AuthenticationPrincipal AccountDto accountDto) {

        messageSaveService.deleteSenderAndReceiver(sender, accountDto.getId());

        return ResponseEntity.ok(Map.of("result", true));
    }
}
