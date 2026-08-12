package org.scoula.remittance.service;

import org.scoula.remittance.dto.BankRemittanceInfoDTO;
import org.scoula.remittance.dto.RemittanceDTO;

public interface RemittanceService {

    boolean sendMoney(RemittanceDTO remittanceDTO);

    BankRemittanceInfoDTO getBankRemittanceInfo(Integer userId);

    boolean refundSettlement(Integer requesterUserId, Integer memberUserId, Integer amount);

    java.util.Map<String, Object> saveReceiptFeed(Integer userId, Integer targetId, String feedTypeStr, String content, String visibilityStr, java.util.List<org.springframework.web.multipart.MultipartFile> files);
}