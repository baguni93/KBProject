package org.scoula.security.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.user.domain.UserVO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    private Long userId;
    private String userName;
    private String phoneNumber;
    private List<String> roles;

    public static UserInfoDTO of(UserVO userVO) {
        return new UserInfoDTO(
                userVO.getUserId(),
                userVO.getUserName(),
                userVO.getPhoneNumber(),
                userVO.getAuthList()
                        .stream()
                        .map(a -> a.getAuth())
                        .toList()
        );
    }
}