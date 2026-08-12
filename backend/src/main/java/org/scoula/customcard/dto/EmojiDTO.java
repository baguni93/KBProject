package org.scoula.customcard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmojiDTO {

    private int id;
    private Double x;
    private Double y;
    private Double rotation;
    private EmojiObjDTO emojiObj;
    private String emojiType;
}
