package org.scoula.customcard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.customcard.domain.CustomCardVO;
import org.scoula.customcard.domain.EmojiVO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomCardSaveRequestDTO {

    private int customCardId;
    private int userId;
    private String pattern;
    private String savedDrawingImage;

    private String backgroundType;
    private String backgroundValue;

    private List<EmojiDTO> emojis;
    private List<TextDTO> texts;


    public static CustomCardSaveRequestDTO of(CustomCardVO customCardVo) {
        return customCardVo == null ? null : CustomCardSaveRequestDTO.builder()
                .customCardId(customCardVo.getCustomCardId())
                .userId(customCardVo.getUserId())
                .pattern(customCardVo.getPatternPath())
                .savedDrawingImage(customCardVo.getDrawingImageUrl())
                .backgroundType(customCardVo.getBackgroundType())
                .backgroundValue(customCardVo.getBackgroundValue())
                .build();
    }
}
