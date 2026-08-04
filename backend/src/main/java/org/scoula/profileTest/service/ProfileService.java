package org.scoula.profileTest.service;

import org.scoula.profileTest.dto.ProfileResponseDTO;

public interface ProfileService {
    ProfileResponseDTO get(int userId);
}
