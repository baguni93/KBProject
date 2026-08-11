package org.scoula.customcard.service;

import org.scoula.customcard.dto.CheckCanIssueDTO;
import org.scoula.customcard.dto.CustomCardAgreementDTO;
import org.scoula.customcard.dto.CustomCardRequestDTO;
import org.scoula.customcard.dto.CustomCardSaveRequestDTO;

import java.util.List;

public interface CustomCardService {
    List<CustomCardAgreementDTO> getAgreements();

    Boolean checkAgreementAgree(int userId);

    void setAgreementAgree(int userId);

    CheckCanIssueDTO checkCanIssue(CheckCanIssueDTO checkCanIssueDTO);

    void applyCard(CustomCardSaveRequestDTO dto);

    CustomCardSaveRequestDTO loadCard(int userId , int customId);
}
