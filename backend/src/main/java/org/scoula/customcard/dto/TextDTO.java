package org.scoula.customcard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextDTO {
    private int id;
    private String text;
    private Double x;
    private Double y;
    private Double rotation;
    private String font;
    private String color;
    private String size;
    private Boolean isBold;
}
