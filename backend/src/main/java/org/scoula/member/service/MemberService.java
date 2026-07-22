package org.scoula.member.service;

import org.scoula.member.dto.ChangePasswordDTO;
import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDTO;
import org.scoula.member.dto.MemberUpdateDTO;

import java.util.List;

public interface MemberService {

    MemberDTO get(String username);

    MemberDTO join(MemberJoinDTO memberJoinDTO);
    boolean checkDuplicate(String username);

    MemberDTO update(MemberUpdateDTO memberUpdateDTO);
    boolean delete(Long id);
    void changePassword(ChangePasswordDTO changePassword);
}
