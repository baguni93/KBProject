package org.scoula.security.account.mapper;

import org.scoula.security.account.domain.AuthVO;
import org.scoula.security.account.domain.MemberVO;

import java.util.List;

public interface UserDetailMapper {

    MemberVO get(String username);
    MemberVO get2(String username);
    List<AuthVO> getAuthList(String name);
}
