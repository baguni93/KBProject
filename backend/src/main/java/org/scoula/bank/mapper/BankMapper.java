package org.scoula.bank.mapper;

import org.scoula.bank.domain.BankVO;

import java.util.List;

public interface BankMapper {

    // 사용 중인 은행 목록 조회
    List<BankVO> findAllActive();
}