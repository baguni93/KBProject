package org.scoula.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinChangeDTO {

    private String currentPinPassword;
    private String newPinPassword;
    private String newPinPasswordConfirm;
}