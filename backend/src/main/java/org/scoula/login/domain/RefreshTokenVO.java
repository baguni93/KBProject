package org.scoula.login.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenVO {

    private Long refreshTokenId;

    private Long userId;

    private String refreshToken;

    private LocalDateTime issuedAt;

    private LocalDateTime expiresAt;
}