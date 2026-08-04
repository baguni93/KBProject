package org.scoula.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupDTO {

    private String userName;

    private LocalDate birthDate;

    private String phoneNumber;

    private String pinPassword;

    private String nickname;

    // 사용자가 동의한 약관 ID
    private List<Long> agreementIds;
}