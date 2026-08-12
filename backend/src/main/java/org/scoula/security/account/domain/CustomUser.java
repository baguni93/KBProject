package org.scoula.security.account.domain;

import lombok.Getter;
import lombok.Setter;
import org.scoula.user.domain.UserVO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
@Setter
public class CustomUser extends User {

    private UserVO user; // 실질적인 사용자 데이터

    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public CustomUser(UserVO vo) {
        super(vo.getPhoneNumber(), vo.getPinPassword(), vo.getAuthList());
        this.user = vo;
    }
}