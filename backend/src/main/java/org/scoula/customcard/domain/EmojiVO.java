package org.scoula.customcard.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmojiVO {

    private int emojiId;
    private int customCardId;
    private String emojiUrl;
    private Double xPos;
    private Double yPos;
    private Double rotation;
    private String emojiType;
}
