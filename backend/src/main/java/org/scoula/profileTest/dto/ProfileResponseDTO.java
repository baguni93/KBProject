package org.scoula.profileTest.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.profileTest.domain.ProfileVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseDTO {

    private int profileId;
    private int userId;
    private String nickname;
    private String introduction;
    private String imageName;

    public static ProfileResponseDTO of(ProfileVO profileVO){
        return profileVO == null ? null : ProfileResponseDTO.builder()
                .profileId(profileVO.getProfileId())
                .userId(profileVO.getUserId())
                .nickname(profileVO.getNickname())
                .introduction(profileVO.getIntroduction())
                .imageName(profileVO.getImageName())
                .build();

    }

    // 프론트엔드에서 사용할 url 프로퍼티
    public String getUrl() {
        return "/api/profile/image/" + imageName;
    }

}
