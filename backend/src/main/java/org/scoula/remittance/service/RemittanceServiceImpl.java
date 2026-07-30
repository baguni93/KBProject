package org.scoula.remittance.service;

import lombok.RequiredArgsConstructor;
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
public class RemittanceServiceImpl implements RemittanceService {

    private final RemittanceMapper remittanceMapper;

    @Override
    @Transactional
    public boolean sendMoney(RemittanceDTO remittanceDTO) {

        int result1 = remittanceMapper.subtractBalance(remittanceDTO.getWalletId(), remittanceDTO.getAmount());
        if (result1 == 0) {
            throw new RuntimeException("지갑 잔액이 부족하거나 지갑 정보를 찾을 수 없습니다.");
        }

        if ("ACCOUNT".equals(remittanceDTO.getReceiverType())) {
            remittanceMapper.addAccountBalance(
                    remittanceDTO.getBankCode(),
                    remittanceDTO.getAccountNumber(),
                    remittanceDTO.getAmount()
            );
        } else {
            remittanceMapper.addBalance(
                    remittanceDTO.getReceiverId(),
                    remittanceDTO.getAmount()
            );
        }

        remittanceDTO.setStatus("SUCCESS");
        remittanceMapper.insertRemittance(remittanceDTO);

        if (remittanceDTO.getFeedType() == null || remittanceDTO.getFeedType().isEmpty()) {
            remittanceDTO.setFeedType("REMITTANCE");
        }
        if (remittanceDTO.getVisibility() == null || remittanceDTO.getVisibility().isEmpty()) {
            remittanceDTO.setVisibility("PUBLIC");
        }
        if (remittanceDTO.getContent() == null || remittanceDTO.getContent().isEmpty()) {
            remittanceDTO.setContent(remittanceDTO.getMemo() != null ? remittanceDTO.getMemo() : "송금 완료");
        }

        if (remittanceDTO.getMemo() != null && !remittanceDTO.getMemo().isEmpty()) {
            remittanceMapper.insertReceiptMemo(remittanceDTO.getTransactionId(), remittanceDTO.getMemo());
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
        if (recentAccounts == null || recentAccounts.size() < 3) {
            if (recentAccounts == null) recentAccounts = new ArrayList<>();
            // 최근 거래 이력이 부족할 경우 기본 3개 더미 계좌 보충
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