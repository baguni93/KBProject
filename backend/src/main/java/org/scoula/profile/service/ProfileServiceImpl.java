package org.scoula.profile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.profile.domain.ProfileVO;
import org.scoula.profile.dto.ProfileResponseDTO;
import org.scoula.profile.mapper.ProfileMapper;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProfileServiceImpl implements  ProfileService{

    private final ProfileMapper profileMapper;

    @Override
    public ProfileResponseDTO get(int userId) {
        ProfileVO vo =   profileMapper.get(userId);

        return ProfileResponseDTO.of(vo);
    }
}
