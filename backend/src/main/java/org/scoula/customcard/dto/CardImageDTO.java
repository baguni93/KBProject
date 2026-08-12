package org.scoula.customcard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardImageDTO {
    private int customCardImageId;
    private int customCardId;
    private String customCardImageName;

}
