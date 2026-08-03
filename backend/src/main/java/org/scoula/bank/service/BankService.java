package org.scoula.bank.service;

import org.scoula.bank.dto.BankDTO;

import java.util.List;

public interface BankService {

    // 은행 목록 조회
    List<BankDTO> getBanks();
}