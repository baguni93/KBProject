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
        if ("ACCOUNT".equals(remittanceDTO.getReceiverType())) {
            remittanceMapper.addAccountBalance(
                    remittanceDTO.getBankCode(),
                    remittanceDTO.getAccountNumber(),
                    amount
            );
            if (remittanceDTO.getReceiverId() == null) {
                remittanceDTO.setReceiverId(walletId);
            }
        } else {
            remittanceMapper.addBalance(
                    remittanceDTO.getReceiverId(),
                    amount
            );
        }

        // 거래 내역 기록
        remittanceDTO.setStatus("SUCCESS");
        remittanceMapper.insertRemittance(remittanceDTO);

        // 지갑/친구 송금 피드 및 이미지 등록
        org.scoula.feed.dto.FeedResponseDTO feedRes = null;
        if (!"ACCOUNT".equalsIgnoreCase(remittanceDTO.getReceiverType())) {
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
                    .targetId(remittanceDTO.getTransactionId())
                    .feedType(Enum.FeedType.TRANSFER)
                    .content(content)
                    .visibility(visibility)
                    .build();

            // 팀원이 작성한 FeedService를 호출하여 피드 생성
            feedRes = feedService.create(feedRequest);

            // 첨부파일 저장 (파일 경로 예외 발생 시 피드 및 송금 거래가 롤백되지 않도록 안전 처리)
            if (feedRes != null && remittanceDTO.getFiles() != null && !remittanceDTO.getFiles().isEmpty()) {
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
        }

        log.info("송금 완료 성공 - ID: {}", remittanceDTO.getTransactionId());
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

    @Override
    @Transactional
    public boolean refundSettlement(Integer requesterUserId, Integer memberUserId, Integer amount) {
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
}