package org.scoula.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.UploadPathName;
import org.scoula.exception.PasswordMismatchException;
import org.scoula.member.dto.ChangePasswordDTO;
import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDTO;
import org.scoula.member.dto.MemberUpdateDTO;
import org.scoula.member.mapper.MemberMapper;
import org.scoula.security.account.domain.AuthVO;
import org.scoula.security.account.domain.MemberVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor

public class MemberServiceImpl implements MemberService{

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    @Override
    public MemberDTO get(String username) {

        MemberDTO dto = MemberDTO.of(memberMapper.get(username));

        return Optional.ofNullable(dto)
                .orElseThrow(NoSuchElementException::new);
    }

    @Transactional
    @Override
    public MemberDTO join(MemberJoinDTO memberJoinDTO) {
        MemberVO member = memberJoinDTO.toVO();

        member.setPassword(passwordEncoder.encode(member.getPassword())); // 비밀번호 암호화
        memberMapper.insert(member);

        AuthVO auth = new AuthVO();
        auth.setUsername(member.getUsername());
        auth.setAuth("ROLE_MEMBER");
        memberMapper.insertAuth(auth);
        saveAvatar(memberJoinDTO.getAvatar(), member.getUsername());
        return get(member.getUsername());

    }

    private void saveAvatar(MultipartFile avatar, String username) {
        //아바타 업로드
        String basePath  = UploadPathName.getUploadPath();

        if(avatar != null && !avatar.isEmpty()) {
            File file = new File(basePath, username + ".png");
            try {
                avatar.transferTo(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public boolean checkDuplicate(String username) {
        MemberVO member = memberMapper.findByUsername(username);
        return member != null ? true : false;
    }

    @Override
    public MemberDTO update(MemberUpdateDTO memberUpdateDTO) {

        MemberVO vo = memberMapper.get(memberUpdateDTO.getUsername());
        if(!passwordEncoder.matches(memberUpdateDTO.getPassword(),vo.getPassword())) { // 비밀번호 일치 확인
            throw new PasswordMismatchException();
        }
        memberMapper.update(memberUpdateDTO.toVO());
        saveAvatar(memberUpdateDTO.getAvatar(), memberUpdateDTO.getUsername());
        return get(memberUpdateDTO.getUsername());
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public void changePassword(ChangePasswordDTO changePassword) {
        MemberVO member = memberMapper.get(changePassword.getUsername());
        if(!passwordEncoder.matches(changePassword.getOldPassword(), member.getPassword())) {
            throw new PasswordMismatchException();
        }
        changePassword.setNewPassword(passwordEncoder.encode(changePassword.getNewPassword()));
        memberMapper.updatePassword(changePassword);

    }
}
