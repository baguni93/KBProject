package org.scoula.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponseDTO {

    private int userId;
    private String userName;
    private String nickname;
    private String profileImageName;
    private boolean friend;
}
