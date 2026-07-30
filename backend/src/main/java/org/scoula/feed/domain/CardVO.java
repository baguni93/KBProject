package org.scoula.feed.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.UploadPathName;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardVO {
    private int applyId;
    private String cardImageName;

    // 프론트엔드에서 사용할 url 프로퍼티
    public String getUrl() {
        return "/api/feeds/cardImage/" + cardImageName;
    }
}
