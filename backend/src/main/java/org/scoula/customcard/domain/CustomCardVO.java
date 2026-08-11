package org.scoula.customcard.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.customcard.dto.EmojiDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomCardVO {

    private int customCardId; // INSERT 후 auto_increment 값 반환용
    private int userId;
    private String backgroundType;
    private String backgroundValue;
    private String patternPath;
    private String drawingImageUrl;
    private Date createdAt;

}
