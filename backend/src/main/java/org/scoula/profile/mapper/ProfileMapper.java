package org.scoula.profile.mapper;

import org.scoula.profile.domain.ProfileVO;

public interface ProfileMapper {
    ProfileVO get(int userId);
}
