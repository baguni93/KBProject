package org.scoula.profile.service;

import org.scoula.profile.dto.ProfileResponseDTO;

public interface ProfileService {
    ProfileResponseDTO get(int userId);
}
