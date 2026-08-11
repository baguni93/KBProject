package org.scoula.customcard.controller;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.annotations.Param;
import org.scoula.common.util.UploadFiles;
import org.scoula.common.util.UploadPathName;
import org.scoula.customcard.dto.CustomCardAgreementDTO;
import org.scoula.customcard.dto.CheckCanIssueDTO;
import org.scoula.customcard.dto.CustomCardSaveRequestDTO;
import org.scoula.customcard.service.CustomCardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/customcard")
@RequiredArgsConstructor
@Log4j2
public class CustomCardController {

    private final CustomCardService customCardService;

    @ApiOperation("카드 발급 약관 목록 조회")
    @GetMapping("/agreements/list")
    public ResponseEntity<List<CustomCardAgreementDTO>> getAgreements() {
        return ResponseEntity.ok(
                customCardService.getAgreements()
        );
    }

    @ApiOperation("카드 발급 약관 동의")
    @PostMapping("/agreements")
    public ResponseEntity<HttpStatus> setAgreementAgree(@RequestParam int userId) {
        customCardService.setAgreementAgree(userId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @ApiOperation("카드 발급 약관 동의 여부 확인")
    @GetMapping("/agreements")
    public ResponseEntity<Boolean> checkAgreementAgree(@RequestParam int userId) {
        return ResponseEntity.ok(
                customCardService.checkAgreementAgree(userId)
        );
    }

    @ApiOperation("계좌의 은행코드 국민 여부, 계좌가 온마이웨이 발급 이력 여부")
    @PostMapping("/agreements/checkCanIssue")
    public ResponseEntity<CheckCanIssueDTO> checkCanIssue(@RequestBody CheckCanIssueDTO checkCanIssueDTO) {
        return ResponseEntity.ok(
                customCardService.checkCanIssue(checkCanIssueDTO)
        );
    }

    @PostMapping("/apply")
    public ResponseEntity<HttpStatus> apply(@RequestBody CustomCardSaveRequestDTO request){
       customCardService.applyCard(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }
    @GetMapping("/load/{customCardId}")
    public ResponseEntity<CustomCardSaveRequestDTO> save(@RequestParam int userId ,@PathVariable int customCardId){

        return ResponseEntity.ok(customCardService.loadCard(userId , customCardId));
    }

    @PostMapping("/uploadImage")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = UploadFiles.uploadAndGetFileName(UploadPathName.getCustomCardPath(), file);
        return ResponseEntity.ok(fileName);
    }

}
