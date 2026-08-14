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
public class FeedCreateRequestDTO  {

    private int userId;
    private int targetId;
    private Enum.FeedType feedType;
    private String content;
    private Enum.VisibilityType visibility;

    @Builder.Default // 사용자가 form에서 업로드한 실제 파일 목록
    private List<MultipartFile> files = new ArrayList<>();


    public FeedVO toVo(){
        return FeedVO.builder()
                .userId(userId)
                .targetId(targetId)
                .feedType(feedType)
                .content(content)
                .visibility(visibility)
                .build();
    }
}
