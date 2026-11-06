package com.koboolean.metagen.chat.config;

import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String USER_CHANNEL_PREFIX = "/sub/chat/";

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat") // 로그인 사용자만 접속 가능 (SecurityConfig anyRequest().authenticated())
                .setAllowedOriginPatterns("*")
                .withSockJS(); // SockJS 사용 시
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub"); // 구독 주소
        registry.setApplicationDestinationPrefixes("/pub"); // 메시지 보낼 때 주소
    }

    /**
     * STOMP 요청 검증 : 로그인 사용자만 메시지를 보낼 수 있고, 본인 채널(/sub/chat/{내 id})만 구독할 수 있다.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null || accessor.getCommand() == null) {
                    return message;
                }

                StompCommand command = accessor.getCommand();

                if (command == StompCommand.SEND || command == StompCommand.SUBSCRIBE) {
                    if (!(accessor.getUser() instanceof Authentication authentication)
                            || !(authentication.getPrincipal() instanceof AccountDto accountDto)) {
                        throw new AccessDeniedException("로그인이 필요합니다.");
                    }

                    if (command == StompCommand.SUBSCRIBE
                            && !(USER_CHANNEL_PREFIX + accountDto.getId()).equals(accessor.getDestination())) {
                        throw new AccessDeniedException("다른 사용자의 채널은 구독할 수 없습니다.");
                    }
                }

                return message;
            }
        });
    }
}
