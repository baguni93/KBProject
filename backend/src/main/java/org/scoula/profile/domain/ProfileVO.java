package org.scoula.profile.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileVO {

    private Long profileId;

    private Long userId;

    private String nickname;

    private String introduction;

    private String originalName;

    private String storedName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}