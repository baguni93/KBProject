package org.scoula.profile.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileVO {

    private int profileId;
    private int userId;
    private String nickname;
    private String introduction;
    private String imageName;


}
