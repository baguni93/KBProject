package org.scoula.remittance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.Enum;
import org.scoula.feed.dto.FeedCreateRequestDTO;
import org.scoula.feed.service.FeedService;
import org.scoula.remittance.dto.BankDTO;
import org.scoula.remittance.dto.BankRemittanceInfoDTO;
import org.scoula.remittance.dto.RecentAccountDTO;
import org.scoula.remittance.dto.RemittanceDTO;
import org.scoula.remittance.mapper.RemittanceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class RemittanceServiceImpl implements RemittanceService {

    private final RemittanceMapper remittanceMapper;
    private final FeedService feedService;

    @Override
    @Transactional
    public boolean sendMoney(RemittanceDTO remittanceDTO) {
        Integer walletId = remittanceDTO.getWalletId();
        Integer amount = remittanceDTO.getAmount();

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("송금 금액은 0원보다 커야 합니다.");
        }

        // 1. 현재 지갑 잔액 조회 및 부족 시 자동 충전
        int currentBalance = remittanceMapper.getWalletBalance(walletId);
        if (currentBalance < amount) {
            int shortage = amount - currentBalance;
            log.info("지갑 잔액 부족 (현재: {}원, 송금액: {}원) -> {}원 자동 충전 진행", currentBalance, amount, shortage);

            // 지갑 잔액 충전 및 충전 거래 이력 기록
            remittanceMapper.addBalance(walletId, shortage);
            remittanceMapper.insertChargeTransaction(walletId, shortage);
        }

        // 2. 내 지갑 잔액 차감
        int subtracted = remittanceMapper.subtractBalance(walletId, amount);
        if (subtracted == 0) {
            throw new RuntimeException("지갑 잔액 차감에 실패했습니다.");
        }

        // 3. 수신처 잔액 증가 (계좌 송금 vs 지갑/친구 송금)
        if ("ACCOUNT".equals(remittanceDTO.getReceiverType())) {
            remittanceMapper.addAccountBalance(
                    remittanceDTO.getBankCode(),
                    remittanceDTO.getAccountNumber(),
                    amount
            );
            // 계좌 송금인 경우 피드 INNER JOIN 바인딩을 위해 receiverId 기본값 지정
            if (remittanceDTO.getReceiverId() == null) {
                remittanceDTO.setReceiverId(walletId);
            }
        } else {
            remittanceMapper.addBalance(
                    remittanceDTO.getReceiverId(),
                    amount
            );
        }

        // 4. 송금 거래 내역 기록 (financial_transaction_tbl INSERT)
        remittanceDTO.setStatus("SUCCESS");
        remittanceMapper.insertRemittance(remittanceDTO);

        // 5. 팀원 요청 반영: FeedService.create()를 호출하여 피드 등록
        String content = (remittanceDTO.getContent() != null && !remittanceDTO.getContent().isEmpty())
                ? remittanceDTO.getContent() : (remittanceDTO.getMemo() != null ? remittanceDTO.getMemo() : "송금 완료");

        if (content.length() > 20) {
            content = content.substring(0, 20);
        }

        Enum.VisibilityType visibility = Enum.VisibilityType.PUBLIC;
        if ("FRIENDS".equalsIgnoreCase(remittanceDTO.getVisibility()) || "FRIEND".equalsIgnoreCase(remittanceDTO.getVisibility())) {
            visibility = Enum.VisibilityType.FRIEND;
        } else if ("PRIVATE".equalsIgnoreCase(remittanceDTO.getVisibility())) {
            visibility = Enum.VisibilityType.PRIVATE;
        }

        FeedCreateRequestDTO feedRequest = FeedCreateRequestDTO.builder()
                .userId(walletId)
                .targetId(remittanceDTO.getTransactionId()) // 생성된 송금 거래 PK (target_id)
                .feedType(Enum.FeedType.TRANSFER)
                .content(content)
                .visibility(visibility)
                .build();

        org.scoula.feed.dto.FeedResponseDTO feedRes = feedService.create(feedRequest);
        System.out.println("========== [송금 디버그] feedService.create() 완료, feedRes: " + feedRes);

        if (feedRes != null) {
            System.out.println("========== [송금 디버그] 생성된 피드 ID: " + feedRes.getFeedId());
        }
        if (remittanceDTO.getFiles() != null) {
            System.out.println("========== [송금 디버그] 전달받은 files 크기: " + remittanceDTO.getFiles().size());
        } else {
            System.out.println("========== [송금 디버그] remittanceDTO.getFiles() 가 null 입니다!");
        }

        // 사용자의 송금 영역 직속 피드 이미지 DB 저장 로직 (feed_image_tbl INSERT)
        if (feedRes != null && remittanceDTO.getFiles() != null && !remittanceDTO.getFiles().isEmpty()) {
            for (org.springframework.web.multipart.MultipartFile part : remittanceDTO.getFiles()) {
                if (part == null || part.isEmpty()) {
                    System.out.println("========== [송금 디버그] MultipartFile part가 empty 상태입니다.");
                    continue;
                }
                try {
                    String uploadPath = org.scoula.common.util.UploadFiles.upload(org.scoula.common.util.UploadPathName.getFeedPath(), part);
                    String savedFileName = new java.io.File(uploadPath).getName();
                    int insertedRow = remittanceMapper.insertFeedImage(feedRes.getFeedId(), savedFileName);
                    System.out.println("========== [송금 디버그] remittanceMapper.insertFeedImage 실행 결과 row: " + insertedRow + ", FeedID: " + feedRes.getFeedId() + ", File: " + savedFileName);
                    log.info("송금 피드 이미지 DB 입력 성공 - FeedID: {}, Image: {}", feedRes.getFeedId(), savedFileName);
                } catch (Exception imgE) {
                    System.out.println("========== [송금 디버그] 이미지 저장 중 예외 발생!");
                    imgE.printStackTrace();
                    log.error("송금 이미지 저장 예외 발생 (송금 거래는 유지): ", imgE);
                }
            }
        }

        log.info("송금 완료 및 FeedService.create() 피드 등록 성공 - 거래 ID: {}, 피드 내용: {}", remittanceDTO.getTransactionId(), content);
        return true;
    }

    @Override
    public BankRemittanceInfoDTO getBankRemittanceInfo(Integer userId) {
        List<BankDTO> banks = remittanceMapper.getBankList();
        if (banks == null || banks.isEmpty()) {
            banks = List.of(
                    BankDTO.builder().bankCode("004").bankName("KB국민은행").build(),
                    BankDTO.builder().bankCode("088").bankName("신한은행").build(),
                    BankDTO.builder().bankCode("020").bankName("우리은행").build(),
                    BankDTO.builder().bankCode("011").bankName("NH농협은행").build(),
                    BankDTO.builder().bankCode("090").bankName("카카오뱅크").build()
            );
        }

        List<RecentAccountDTO> recentAccounts = remittanceMapper.getRecentAccounts(userId, 3);
        if (recentAccounts == null || recentAccounts.size() < 3) {
            if (recentAccounts == null) recentAccounts = new ArrayList<>();
            if (recentAccounts.stream().noneMatch(a -> "222-002-000001".equals(a.getAccountNumber()))) {
                recentAccounts.add(RecentAccountDTO.builder()
                        .bankCode("088").bankName("신한은행").accountNumber("222-002-000001").ownerName("이KB").lastTransferAt(new Date()).build());
            }
            if (recentAccounts.size() < 3 && recentAccounts.stream().noneMatch(a -> "110-111-111111".equals(a.getAccountNumber()))) {
                recentAccounts.add(RecentAccountDTO.builder()
                        .bankCode("004").bankName("KB국민은행").accountNumber("110-111-111111").ownerName("김국민").lastTransferAt(new Date()).build());
            }
            if (recentAccounts.size() < 3 && recentAccounts.stream().noneMatch(a -> "1002-345-6789".equals(a.getAccountNumber()))) {
                recentAccounts.add(RecentAccountDTO.builder()
                        .bankCode("020").bankName("우리은행").accountNumber("1002-345-6789").ownerName("박스타").lastTransferAt(new Date()).build());
            }
            if (recentAccounts.size() > 3) {
                recentAccounts = recentAccounts.subList(0, 3);
            }
        }

        return BankRemittanceInfoDTO.builder()
                .banks(banks)
                .recentAccounts(recentAccounts)
                .build();
    }
}