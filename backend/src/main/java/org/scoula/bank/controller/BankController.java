package org.scoula.bank.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.bank.dto.BankDTO;
import org.scoula.bank.service.BankService;
import org.scoula.common.util.UploadFiles;
import org.scoula.common.util.UploadPathName;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.io.File;

@RestController
@RequestMapping("/api/banks")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    // BANK-001 은행 목록 조회
    @GetMapping
    public ResponseEntity<List<BankDTO>> getBanks() {
        return ResponseEntity.ok(bankService.getBanks());
    }

    // 은행 로고 조회
    @GetMapping("/logo/{fileName}")
    public void getBankLogo(@PathVariable String fileName, HttpServletResponse response) {
        File logoFile = new File(UploadPathName.getBankPath(), fileName);

        if (!logoFile.exists() || !logoFile.isFile()) {
            throw new IllegalArgumentException("은행 로고를 찾을 수 없습니다.");
        }

        UploadFiles.downloadImage(response, logoFile);
    }
}