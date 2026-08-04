package org.scoula.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.user.domain.UserVO;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    private Long userId;

    private String userName;

    private LocalDate birthDate;

    private String phoneNumber;

    private String nickname;

    private String userStatus;

    private LocalDateTime createdAt;

    public static UserInfoDTO of(UserVO user) {
        return UserInfoDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .birthDate(user.getBirthDate())
                .phoneNumber(user.getPhoneNumber())
                .nickname(user.getNickname())
                .userStatus(user.getUserStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}