package org.scoula.remittance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.Enum;
import org.scoula.feed.dto.FeedCreateRequestDTO;
import org.scoula.feed.service.FeedService;
import org.scoula.pointwallet.service.RandomBoxService;
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
    private final RandomBoxService randomBoxService;

    @Override
    @Transactional
    public boolean sendMoney(RemittanceDTO remittanceDTO) {
        Integer walletId = remittanceDTO.getWalletId();
        Integer amount = remittanceDTO.getAmount();

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("송금 금액은 0원보다 커야 합니다.");
        }

        // 잔액 부족 시 자동 충전
        int currentBalance = remittanceMapper.getWalletBalance(walletId);
        if (currentBalance < amount) {
            int shortage = amount - currentBalance;
            log.info("지갑 잔액 부족 -> {}원 자동 충전", shortage);

            remittanceMapper.addBalance(walletId, shortage);
            remittanceMapper.insertChargeTransaction(walletId, shortage);
        }

        // 지갑 잔액 차감
        int subtracted = remittanceMapper.subtractBalance(walletId, amount);

        if (subtracted == 0) {
            throw new RuntimeException("지갑 잔액 차감 실패");
        }


        // 수신처 입금 (계좌 vs 지갑)
        // 박우진 추가 정산은 바로 돈이 입급되지않는다.
        if ("ACCOUNT".equals(remittanceDTO.getReceiverType())) {

            try {
                remittanceMapper.addAccountBalance(
                        remittanceDTO.getBankCode(),
                        remittanceDTO.getAccountNumber(),
                        amount
                );
            } catch (Exception accErr) {
                log.warn("더미 계좌 입금 처리 예외 (계좌 송금 계속 진행): {}", accErr.getMessage());
            }

            remittanceDTO.setReceiverId(null);

        } else {


            Integer recId = remittanceDTO.getReceiverId();
            if (recId == null || recId <= 0) {
                recId = 2; // 테스트용 기본 친구 ID
                remittanceDTO.setReceiverId(recId);
            }
            try {

                remittanceMapper.addBalance(recId, amount);

            } catch (Exception balErr) {
                log.warn("친구 지갑 입금 처리 예외 (송금 계속 진행): {}", balErr.getMessage());
            }
        }

        // 거래 내역 기록
        if (remittanceDTO.getReceiverName() != null && !remittanceDTO.getReceiverName().isEmpty()) {
            remittanceDTO.setMerchantName(remittanceDTO.getReceiverName());
        }
        remittanceDTO.setStatus("SUCCESS");
        remittanceMapper.insertRemittance(remittanceDTO);

        if(remittanceDTO.isSettlement()){
            //정산 지불일떄는 피드와 ,랜덤 박스는 지급하지않는다.
            return true;
        }

        // 지갑/친구/계좌 모든 송금 성공 시 피드 및 이미지 등록
        org.scoula.feed.dto.FeedResponseDTO feedRes = null;
        boolean hasFiles = remittanceDTO.getFiles() != null && !remittanceDTO.getFiles().isEmpty();
        try {
            String content = (remittanceDTO.getContent() != null && !remittanceDTO.getContent().isEmpty())
                    ? remittanceDTO.getContent() : (remittanceDTO.getMemo() != null && !remittanceDTO.getMemo().isEmpty() ? remittanceDTO.getMemo() : "송금 완료!");

            if (content.length() > 50) {
                content = content.substring(0, 50);
            }

            Enum.VisibilityType visibility = Enum.VisibilityType.PUBLIC;
            if ("FRIENDS".equalsIgnoreCase(remittanceDTO.getVisibility()) || "FRIEND".equalsIgnoreCase(remittanceDTO.getVisibility())) {
                visibility = Enum.VisibilityType.FRIEND;
            } else if ("PRIVATE".equalsIgnoreCase(remittanceDTO.getVisibility())) {
                visibility = Enum.VisibilityType.PRIVATE;
            }

            FeedCreateRequestDTO feedRequest = FeedCreateRequestDTO.builder()
                    .userId(walletId)
                    .targetId(remittanceDTO.getTransactionId())
                    .feedType(Enum.FeedType.TRANSFER)
                    .content(content)
                    .visibility(visibility)
                    .build();

            // 팀원이 작성한 FeedService를 호출하여 피드 생성
            feedRes = feedService.create(feedRequest);
            if (feedRes != null) {
                remittanceDTO.setFeedId(feedRes.getFeedId());
                log.info("송금 피드 자동 생성 완료 - feedId={}", feedRes.getFeedId());
            }
        } catch (Exception fErr) {
            log.error("송금 피드 생성 연동 중 예외: ", fErr);
        }

            // 첨부파일 저장 (c:/upload/feed/ 경로에 실제 파일 저장 및 DB 등록)
            if (feedRes != null && hasFiles) {
                for (org.springframework.web.multipart.MultipartFile part : remittanceDTO.getFiles()) {
                    if (part == null || part.isEmpty()) {
                        continue;
                    }
                    try {
                        String uploadPath = org.scoula.common.util.UploadFiles.upload(org.scoula.common.util.UploadPathName.getFeedPath(), part);
                        String savedFileName = new java.io.File(uploadPath).getName();
                        remittanceMapper.insertFeedImage(feedRes.getFeedId(), savedFileName);
                    } catch (Exception imgE) {
                        log.error("피드 이미지 저장 실패 (피드 글과 송금 거래는 정상 저장됨): ", imgE);
                    }
                }
            }

        log.info("송금 완료 성공 - ID: {}", remittanceDTO.getTransactionId());

        // 송금 성공 보상: 송금 완료 건마다 랜덤박스 발급
        try {
            Integer txId = remittanceDTO.getTransactionId();
            if (txId == null || txId <= 0) {
                txId = Math.abs((int)(System.currentTimeMillis() % 100000000)) + 1;
            }

            // DB fk_random_box_target_wallet 외래키 제약조건(wallet_tbl 참조) 준수를 위한 지갑 ID 전달
            Integer targetAccId = (remittanceDTO.getReceiverId() != null && remittanceDTO.getReceiverId() > 0) 
                    ? remittanceDTO.getReceiverId() : walletId;

            randomBoxService.issueForTransfer(walletId, txId, targetAccId);
            log.info("송금 성공 보상 랜덤박스 발급 완료 - userId={}, txId={}, targetAccId={}", walletId, txId, targetAccId);
        } catch (Throwable rBoxErr) {
            log.warn("송금 성공 후 랜덤박스 발급 처리 중 예외 (송금은 정상 완료): {}", rBoxErr.getMessage());
        }

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
        if (recentAccounts == null) {
            recentAccounts = new ArrayList<>();
        }

        return BankRemittanceInfoDTO.builder()
                .banks(banks)
                .recentAccounts(recentAccounts)
                .build();
    }

    @Override
    @Transactional
    public boolean refundSettlement(Integer settlementId,  Integer requesterUserId, Integer memberUserId, Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("환불 금액은 0원보다 커야 합니다.");
        }

        // 1. 방장(정산 요청자) 지갑 잔액 차감
        int subtracted = remittanceMapper.subtractBalance(requesterUserId, amount);
        if (subtracted == 0) {
            throw new RuntimeException("정산 요청자 지갑 잔액 차감에 실패했습니다. (잔액 부족 등)");
        }

        // 2. 환불받을 참여자 지갑 잔액 증가
        int added = remittanceMapper.addBalance(memberUserId, amount);
        if (added == 0) {
            throw new RuntimeException("참여자 지갑 잔액 환불 입금에 실패했습니다.");
        }

        // 3. 거래 내역 기록 (financial_transaction_tbl INSERT)
        RemittanceDTO refundDTO = RemittanceDTO.builder()
                .walletId(requesterUserId)
                .receiverId(memberUserId)
                .amount(amount)
                .status("CANCELLED")
                .receiverType("WALLET")
                .memo("정산 환불")
                .build();

        remittanceMapper.insertRemittance(refundDTO);

        log.info("정산 환불 처리 성공 - 방장 ID: {}, 수신자 ID: {}, 금액: {}원", requesterUserId, memberUserId, amount);
        return true;
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> saveReceiptFeed(Integer userId, Integer targetId, String feedTypeStr, String content, String visibilityStr, List<org.springframework.web.multipart.MultipartFile> files) {
        Enum.VisibilityType visibility = Enum.VisibilityType.PUBLIC;
        if ("FRIENDS".equalsIgnoreCase(visibilityStr) || "FRIEND".equalsIgnoreCase(visibilityStr)) {
            visibility = Enum.VisibilityType.FRIEND;
        } else if ("PRIVATE".equalsIgnoreCase(visibilityStr)) {
            visibility = Enum.VisibilityType.PRIVATE;
        }

        Enum.FeedType fType = Enum.FeedType.PAYMENT;
        if (feedTypeStr != null) {
            try {
                fType = Enum.FeedType.valueOf(feedTypeStr.toUpperCase());
            } catch (Exception e) {
                fType = Enum.FeedType.PAYMENT;
            }
        }

        FeedCreateRequestDTO feedRequest = FeedCreateRequestDTO.builder()
                .userId(userId != null ? userId : 1)
                .targetId(targetId)
                .feedType(fType)
                .content(content)
                .visibility(visibility)
                .build();

        org.scoula.feed.dto.FeedResponseDTO feedRes = feedService.create(feedRequest);

        if (feedRes != null && files != null && !files.isEmpty()) {
            for (org.springframework.web.multipart.MultipartFile part : files) {
                if (part != null && !part.isEmpty()) {
                    try {
                        String uploadPath = org.scoula.common.util.UploadFiles.upload(org.scoula.common.util.UploadPathName.getFeedPath(), part);
                        String savedFileName = new java.io.File(uploadPath).getName();
                        remittanceMapper.insertFeedImage(feedRes.getFeedId(), savedFileName);
                    } catch (Exception imgE) {
                        log.error("결제 내역 피드 이미지 저장 예외: ", imgE);
                    }
                }
            }
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("feedId", feedRes != null ? feedRes.getFeedId() : null);
        return result;
    }
}