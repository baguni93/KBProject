package org.scoula.pointwallet.service;

import org.scoula.pointwallet.dto.PointTransactionDTO;
import org.scoula.pointwallet.dto.PointWalletDTO;

import java.util.List;

public interface PointWalletService {

    PointWalletDTO getWallet(Integer userId);
    List<PointTransactionDTO> getTransactions(Integer userId);
}