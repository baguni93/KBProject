package org.scoula.login.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupStatusResponseDTO {

    private String memberStatus;
    private boolean existingMember;
    private LocalDateTime rejoinAvailableAt;
}