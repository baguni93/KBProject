package org.scoula.transaction.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.transaction.dto.TransactionDTO;

import java.util.List;

@Mapper
public interface TransactionMapper {

    List<TransactionDTO> getTransactionList(@Param("userId") Integer userId, @Param("type") String type);

    TransactionDTO getTransactionById(@Param("transactionId") Integer transactionId);

    int updateReceiptMemo(@Param("transactionId") Integer transactionId, @Param("memo") String memo);

    int insertReceiptMemoIfAbsent(@Param("transactionId") Integer transactionId, @Param("memo") String memo);
}
