package org.scoula.login.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenDTO {

    private Long userId;
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private long accessTokenExpiresIn;
}