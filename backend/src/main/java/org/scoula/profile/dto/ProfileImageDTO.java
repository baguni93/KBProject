package org.scoula.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileImageDTO {

    private Long userId;

    private String originalName;

    private String storedName;

    private String profileImageUrl;
}