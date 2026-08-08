package org.scoula.remittance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemittanceDTO {

    private Integer walletId;
    private Integer receiverId;
    private Integer amount;
    private String memo;
    private String status;

    private String receiverType;
    private String bankCode;
    private String accountNumber;

    private Integer transactionId;

    // 피드 연동용 필드
    private String feedType;    // REMITTANCE
    private String content;     // 피드 내용
    private String visibility;  // PUBLIC, FRIENDS, PRIVATE
    private Integer feedId;     // 생성된 피드 ID

    // 피드 사진 첨부용 필드 (JSON 직렬화 시 InputStream 오류 방지)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<MultipartFile> files;
}
