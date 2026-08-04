package org.scoula.profileTest.mapper;

import org.scoula.profileTest.domain.ProfileVO;

public interface ProfileTestMapper {
    ProfileVO get(int userId);
}
