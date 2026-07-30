package org.scoula.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.transaction.dto.TransactionDTO;
import org.scoula.transaction.mapper.TransactionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class TransactionServiceImpl implements TransactionService {

    private final TransactionMapper transactionMapper;

    @Override
    public List<TransactionDTO> getTransactionList(Integer userId, String type) {
        return transactionMapper.getTransactionList(userId, type);
    }

    @Override
    public TransactionDTO getTransactionById(Integer transactionId) {
        return transactionMapper.getTransactionById(transactionId);
    }

    @Override
    public boolean updateMemo(Integer transactionId, String memo) {
        int updated = transactionMapper.updateReceiptMemo(transactionId, memo);
        if (updated == 0) {
            updated = transactionMapper.insertReceiptMemoIfAbsent(transactionId, memo);
        }
        return updated > 0;
    }
}
