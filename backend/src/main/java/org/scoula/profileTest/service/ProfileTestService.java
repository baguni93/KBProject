package org.scoula.profileTest.service;

import org.scoula.profileTest.dto.ProfileResponseDTO;

public interface ProfileTestService {
    ProfileResponseDTO get(int userId);
}
