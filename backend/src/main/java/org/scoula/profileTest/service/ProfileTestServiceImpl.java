package org.scoula.profileTest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.profileTest.domain.ProfileVO;
import org.scoula.profileTest.dto.ProfileResponseDTO;
import org.scoula.profileTest.mapper.ProfileTestMapper;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProfileTestServiceImpl implements ProfileTestService {

    private final ProfileTestMapper profileTestMapper;

    @Override
    public ProfileResponseDTO get(int userId) {
        ProfileVO vo =   profileTestMapper.get(userId);

        return ProfileResponseDTO.of(vo);
    }
}
