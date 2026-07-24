package org.scoula.pointwallet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.pointwallet.dto.PointTransactionDTO;
import org.scoula.pointwallet.dto.PointWalletDTO;
import org.scoula.pointwallet.mapper.PointWalletMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class PointWalletServiceImpl implements PointWalletService {

    // 포인트 지갑 매퍼
    private final PointWalletMapper pointWalletMapper;

    // 사용자의 포인트 지갑 가져오기
    @Override
    public PointWalletDTO getWallet(Integer userId) {

        PointWalletDTO wallet =
                pointWalletMapper.selectWalletByUserId(userId);

        if (wallet == null) {
            throw new IllegalArgumentException(
                    "포인트 지갑이 존재하지 않습니다."
            );
        }

        return wallet;
    }

    // 사용자의 거래 내역 가져오기.
    @Override
    public List<PointTransactionDTO> getTransactions(
            Integer userId
    ) {
        /*
         * 거래내역 조회 전에 해당 사용자의 포인트 지갑이
         * 실제로 존재하는지 검증한다. -> 포인트 지갑도 동의 받아야하나? 아님 회원가입시 로직을 처리해야하나.
         */
        PointWalletDTO wallet =
                pointWalletMapper.selectWalletByUserId(userId);

        if (wallet == null) {
            throw new IllegalArgumentException(
                    "포인트 지갑이 존재하지 않습니다."
            );
        }

        List<PointTransactionDTO> transactions =
                pointWalletMapper.selectTransactionsByUserId(userId);

        log.info(
                "포인트 거래내역 조회 userId={}, count={}",
                userId,
                transactions.size()
        );

        return transactions;
    }
}