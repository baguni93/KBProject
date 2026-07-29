package org.scoula.feed.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.UploadPathName;
import org.scoula.feed.domain.FeedImageVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedImageDTO {

    private int imageId;
    private String imageName;

    public static FeedImageDTO of(FeedImageVO vo) {
        if(vo == null){
            return null;
        }

        return FeedImageDTO.builder()
                .imageId(vo.getImageId())
                .imageName(vo.getImageName())
                .build();
    }

    @JsonIgnore // JSON 변환에서 제외
    public String getPath() {
        return UploadPathName.getFeedPath() + imageName;
    }

    // 프론트엔드에서 사용할 url 프로퍼티
    public String getUrl() {
        return "/api/feeds/image/" + imageId;
    }
}
