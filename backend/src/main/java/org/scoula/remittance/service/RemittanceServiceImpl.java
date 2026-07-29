package org.scoula.remittance.service;

import lombok.RequiredArgsConstructor;
import org.scoula.remittance.dto.RemittanceDTO;
import org.scoula.remittance.mapper.RemittanceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemittanceServiceImpl implements RemittanceService {

    private final RemittanceMapper remittanceMapper;

    @Override
    @Transactional
    public boolean sendMoney(RemittanceDTO remittanceDTO) {

        // 공통: 내 지갑 잔액 차감 (출발점)
        int result1 = remittanceMapper.subtractBalance(remittanceDTO.getWalletId(), remittanceDTO.getAmount());
        if (result1 == 0) {
            // 잔액이 부족하거나 지갑이 없으면 예외 발생하여 롤백!
            throw new RuntimeException("지갑 잔액이 부족하거나 지갑 정보를 찾을 수 없습니다.");
        }

        // 조건 분기: 받는 사람 방식에 따라 처리
        if ("ACCOUNT".equals(remittanceDTO.getReceiverType())) {
            // 계좌 송금인 경우 -> 상대방 은행 계좌 잔액 증가
            remittanceMapper.addAccountBalance(
                    remittanceDTO.getBankCode(),
                    remittanceDTO.getAccountNumber(),
                    remittanceDTO.getAmount()
            );
        } else {
            // 지갑 송금인 경우 -> 상대방 지갑 잔액 증가
            remittanceMapper.addBalance(
                    remittanceDTO.getReceiverId(),
                    remittanceDTO.getAmount()
            );
        }

        // 거래 내역 기록 저장
        remittanceMapper.insertRemittance(remittanceDTO);

        //거래 내역 저장
        remittanceMapper.insertRemittance(remittanceDTO);

        //메모가 있으면 영수증 메모 테이블에도 저장!
        if (remittanceDTO.getMemo() != null && !remittanceDTO.getMemo().isEmpty()) {
            remittanceMapper.insertReceiptMemo(remittanceDTO.getTransactionId(), remittanceDTO.getMemo());
        }

        return true;
    }
}