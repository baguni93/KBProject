package org.scoula.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.profile.domain.ProfileVO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {

    private Long profileId;
    private Long userId;
    private String nickname;
    private String introduction;
    private String profileImageUrl;

    public static ProfileDTO of(ProfileVO profile) {
        String profileImageUrl = "/api/users/" + profile.getUserId() + "/profile/image";

        return ProfileDTO.builder()
                .profileId(profile.getProfileId())
                .userId(profile.getUserId())
                .nickname(profile.getNickname())
                .introduction(profile.getIntroduction())
                .profileImageUrl(profileImageUrl)
                .build();
    }
}