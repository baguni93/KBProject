package org.scoula.customcard.service;

import org.scoula.customcard.dto.*;

import java.util.List;

public interface CustomCardService {
    List<CustomCardAgreementDTO> getAgreements();

    Boolean checkAgreementAgree(int userId);

    void setAgreementAgree(int userId);

    CheckCanIssueDTO checkCanIssue(CheckCanIssueDTO checkCanIssueDTO);

    int applyCard(CustomCardSaveRequestDTO dto);

    CustomCardSaveRequestDTO loadCard(int userId , int customId);

}
