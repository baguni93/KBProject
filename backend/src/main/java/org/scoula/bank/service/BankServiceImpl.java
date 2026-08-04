package org.scoula.bank.service;

import lombok.RequiredArgsConstructor;
import org.scoula.bank.dto.BankDTO;
import org.scoula.bank.mapper.BankMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {

    private final BankMapper bankMapper;

    // 은행 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<BankDTO> getBanks() {
        return bankMapper.findAllActive().stream()
                .map(BankDTO::of)
                .collect(Collectors.toList());
    }
}