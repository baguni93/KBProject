package org.scoula.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.security.account.domain.CustomUser;
import org.scoula.security.util.JwtProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;

@Configuration// Spring 설정 클래스 등록
@EnableWebSocketMessageBroker// STOMP 기반 WebSocket 메시지 브로커 활성화
@Log4j2
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtProcessor jwtProcessor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        // 클라이언트 → 서버 방향으로 들어오는 STOMP 메시지 채널에
        // Interceptor를 등록
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(
                    Message<?> message,
                    MessageChannel channel
            ) {

                // 전달받은 STOMP 메시지에서 헤더 정보를 추출
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(
                                message,
                                StompHeaderAccessor.class
                        );

                // WebSocket 최초 연결 요청(CONNECT) 시점인지 확인
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                    // Vue에서 CONNECT 헤더로 전달한 userId 가져오기
                    // 예)
                    // connectHeaders: {
                    //     userId: "1"
                    // }
                    String authorization = accessor
                            .getFirstNativeHeader("Authorization");

                    // userId가 존재하면 WebSocket 사용자 정보 등록
                    if (authorization != null && authorization.startsWith("Bearer ")) {

                        String token = authorization.substring(7);

                        if (jwtProcessor.validateToken(token)) {

                            Long userId = jwtProcessor.getUserId(token);

                            accessor.setUser(
                                    new UsernamePasswordAuthenticationToken(
                                            String.valueOf(userId),
                                            null,
                                            Collections.emptyList()
                                    )
                            );
                        }
                    }
                }

                // 메시지 처리 계속 진행
                return message;
            }

        });

    }

    /**
     * 클라이언트가 최초로 WebSocket에 연결할 엔드포인트를 등록
     * Vue에서는 SockJS('/ws')로 접속하게 된다.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                // WebSocket 연결 주소
                // ex) http://localhost:8080/ws
                .addEndpoint("/ws")

                // CORS 허용 (개발 환경에서는 "*" 사용 가능)
                .setAllowedOriginPatterns("*");

    }

    /**
     * 메시지 송수신 규칙 설정
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지를 구독(subscribe)하는 주소(prefix)
        // "/topic" : 여러 명이 함께 받는 공용 채널
        // "/queue" : 특정 사용자에게 보내는 개인 채널
        registry.enableSimpleBroker("/topic", "/queue");

        // 클라이언트가 서버로 메시지를 보낼 때 사용하는 prefix
        // ex) client.publish("/app/chat")
        // -> @MessageMapping("/chat") 메서드로 전달된다.
        registry.setApplicationDestinationPrefixes("/app");

        // 특정 사용자에게 메시지를 보낼 때 사용하는 prefix
        // convertAndSendToUser("3", "/queue/notifications", ...)
        // ↓ 실제 클라이언트에서는
        // subscribe("/user/queue/notifications")
        registry.setUserDestinationPrefix("/user");
    }
}
