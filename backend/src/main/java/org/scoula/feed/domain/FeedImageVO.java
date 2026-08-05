package org.scoula.feed.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class FeedImageVO {

    private int feedId;
    private int imageId;
    private String imageName;


    // MultipartFile 정보를 첨부파일 VO로 변환
    public static FeedImageVO of(String fileName, int feedId ) {  // path: 업로드된파일경로
        return FeedImageVO.builder()
                .feedId(feedId)
                .imageName(fileName)
                .build();

    }
}
