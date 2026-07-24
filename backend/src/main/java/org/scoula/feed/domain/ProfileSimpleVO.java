package org.scoula.feed.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSimpleVO {
    private int profileId;
    private String nickname;
    private String profileImageName;
}
