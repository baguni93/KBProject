package org.scoula.customcard.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.feed.domain.FeedImageVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardImageVO {

    private int customCardImageId;
    private int customCardId;
    private String customCardImageName;

    // MultipartFile 정보를 첨부파일 VO로 변환
    public static CardImageVO of(String fileName, int customCardId ) {  // path: 업로드된파일경로
        return CardImageVO.builder()
                .customCardId(customCardId)
                .customCardImageName(fileName)
                .build();

    }
}
