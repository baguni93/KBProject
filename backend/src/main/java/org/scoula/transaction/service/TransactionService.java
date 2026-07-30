package org.scoula.transaction.service;

import org.scoula.transaction.dto.TransactionDTO;

import java.util.List;

public interface TransactionService {

    List<TransactionDTO> getTransactionList(Integer userId, String type);

    TransactionDTO getTransactionById(Integer transactionId);

    boolean updateMemo(Integer transactionId, String memo);
}
