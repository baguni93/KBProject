package org.scoula.customcard.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextVO {

    private int textId;
    private int customCardId;
    private String content;
    private Double xPos;
    private Double yPos;
    private Double rotation;
    private String fontFamily;
    private String fontColor;
    private String fontSize;
    private Boolean isBold;
}
