package org.scoula.security.account.mapper;

import org.scoula.security.account.domain.AuthVO;
import org.scoula.user.domain.UserVO;

import java.util.List;

public interface UserDetailMapper {

    UserVO get(String username);
    UserVO get2(String username);
    List<AuthVO> getAuthList(Long userId);
}
