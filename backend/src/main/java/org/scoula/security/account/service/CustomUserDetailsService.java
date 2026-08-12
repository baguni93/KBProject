package org.scoula.security.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.security.account.domain.CustomUser;
import org.scoula.security.account.mapper.UserDetailMapper;
import org.scoula.user.domain.UserVO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDetailMapper mapper;

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        MemberVO vo = mapper.get(username);
//        if(vo == null) {
//            throw new UsernameNotFoundException(username + "은 없는 id입니다.");
//        }
//        return new CustomUser(vo);

        UserVO vo = mapper.get2(username);
        if (vo == null) {
            throw new UsernameNotFoundException(username + "은 없는 id입니다.");
        }

        vo.setAuthList(mapper.getAuthList(vo.getUserId()));

        return new CustomUser(vo);
//
//        return Optional.ofNullable(mapper.get2(username))
//                .map(x -> {
//                    x.setAuthList(mapper.getAuthList(username));
//                    return new CustomUser(x);}).orElseThrow(()-> new UsernameNotFoundException("ㅇ"));
    }
}
