package org.scoula.customcard.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.customcard.domain.*;
import org.scoula.customcard.dto.CheckCanIssueDTO;

import java.util.List;

public interface CustomCardMapper {
    List<CustomCardAgreementVO> getAgreements();

    Boolean checkAgreementAgree(int userId);

    void setAgreementAgree(int userId);

    CheckCanIssueVO checkBank(CheckCanIssueDTO checkCanIssueDTO);

    void insertCard(CustomCardVO cardVo);

    void insertTexts(@Param("textList") List<TextVO> textList);

    void insertEmojis(@Param("emojiList")List<EmojiVO> emojiList);

    CustomCardVO getCard(@Param("userId") int userId,@Param("customCardId") int customCardId);

    List<TextVO> getTexts(int customCardId);

    List<EmojiVO> getEmojis(int customCardId);

    void insertHistory(int customCardId);
}
