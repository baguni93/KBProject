package org.scoula.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;
import org.scoula.feed.domain.FeedVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedUpdateRequestDTO {


    private int feedId;
    private String content;
    private Enum.VisibilityType visibility;

    @Builder.Default // 사용자가 form에서 업로드한 실제 파일 목록
    private List<MultipartFile> files = new ArrayList<>();

    @Builder.Default
    private List<Integer> deleteFiles = new ArrayList<>();

    public FeedVO toVo(){
        return FeedVO.builder()
                .feedId(feedId)
                .content(content)
                .visibility(visibility)
                .build();
    }


}
