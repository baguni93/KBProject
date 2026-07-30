package org.scoula.auth.service;

import org.scoula.auth.dto.PinVerifyRequestDTO;
import org.scoula.auth.dto.PinVerifyResponseDTO;

public interface AuthService {
    PinVerifyResponseDTO verifyPin(PinVerifyRequestDTO requestDTO);
}
