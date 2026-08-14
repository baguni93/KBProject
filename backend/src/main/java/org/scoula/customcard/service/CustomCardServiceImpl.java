package org.scoula.customcard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.account.domain.AccountVerificationVO;
import org.scoula.account.mapper.AccountMapper;
import org.scoula.card.dto.CardCustomCreateDTO;
import org.scoula.card.service.CardService;
import org.scoula.common.util.UploadFiles;
import org.scoula.common.util.UploadPathName;
import org.scoula.customcard.domain.*;
import org.scoula.customcard.dto.*;
import org.scoula.customcard.mapper.CustomCardMapper;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.event.service.EventService;
import org.scoula.feed.domain.FeedImageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class CustomCardServiceImpl implements CustomCardService {

    private final CustomCardMapper customCardMapper;
    private final AccountMapper accountMapper;
    private final SecureRandom secureRandom = new SecureRandom();
    private final CardService cardService;
    private final EventService eventService;

    @Override
    public List<CustomCardAgreementDTO> getAgreements() {

        List<CustomCardAgreementVO> list = customCardMapper.getAgreements();
        return list.stream().map(CustomCardAgreementDTO::of).toList();
    }

    @Override
    public Boolean checkAgreementAgree(int userId) {

        return customCardMapper.checkAgreementAgree(userId);
    }

    @Override
    public void setAgreementAgree(int userId) {
        customCardMapper.setAgreementAgree(userId);
    }

    @Override
    public CheckCanIssueDTO checkCanIssue(CheckCanIssueDTO checkCanIssueDTO) {


        CheckCanIssueVO vo = customCardMapper.checkBank(checkCanIssueDTO);

        if(vo== null){
            throw new CustomException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        String verificationCode = String.format("%04d", secureRandom.nextInt(10000));


        AccountVerificationVO verification = AccountVerificationVO.builder()
                .userId((long)vo.getUserId())
                .bankCode(vo.getBankCode())
                .accountNumber(checkCanIssueDTO.getAccountNumber().trim())
                .accountHolder(vo.getUserName())
                .verificationCode(verificationCode)
                .verifiedYn("N")
                .requestedAt(LocalDateTime.now())
                .build();

        accountMapper.insertVerification(verification);

        return CheckCanIssueDTO.of(vo , verification.getVerificationId() , verificationCode);
    }

    @Override
    public int applyCard(CustomCardSaveRequestDTO dto) {

        CustomCardVO cardVo = CustomCardVO.builder()
                .userId(dto.getUserId())
                .backgroundType(dto.getBackgroundType())
                .backgroundValue(dto.getBackgroundValue())
                .patternPath(dto.getPattern())
                .cardChip(dto.getCardChip())
                .drawingImageUrl(dto.getSavedDrawingImage())
                .build();

        // 2. 메인 카드 INSERT (실행 후 dto.getCustomCardId()에 자동 증가 ID가 담김)
        customCardMapper.insertCard(cardVo);
        int customCardId = cardVo.getCustomCardId();


        if (dto.getTexts() != null && !dto.getTexts().isEmpty()) {
            List<TextVO> textVos = dto.getTexts().stream()
                    .map(t -> TextVO.builder()
                            .customCardId(customCardId)
                            .content(t.getText())
                            .xPos(t.getX())
                            .yPos(t.getY())
                            .rotation(t.getRotation())
                            .fontFamily(t.getFont())
                            .fontColor(t.getColor())
                            .fontSize(t.getSize())
                            .isBold(t.getIsBold())
                            .build())
                    .collect(Collectors.toList());

            customCardMapper.insertTexts(textVos);
        }


        if (dto.getEmojis() != null && !dto.getEmojis().isEmpty()) {
            List<EmojiVO> emojiVos = dto.getEmojis().stream()
                    .map(e -> {
                        String url = (e.getEmojiObj() != null) ? e.getEmojiObj().getEmoji() : null;

                        return EmojiVO.builder()
                                .emojiId(e.getId())
                                .customCardId(customCardId)
                                .emojiUrl(url)
                                .xPos(e.getX())
                                .yPos(e.getY())
                                .rotation(e.getRotation())
                                .emojiType(e.getEmojiType())
                                .build();
                    })
                    .collect(Collectors.toList());

            customCardMapper.insertEmojis(emojiVos);
        }

        //신청 이력 내역 추가
        customCardMapper.insertHistory(customCardId);



        CardImageVO cardImageVO = CardImageVO.of(dto.getCardImageName(),  customCardId);
        customCardMapper.createCustomCardImage(cardImageVO);

        //카드 발급 승인
        cardService.createCardMasterCustom(CardCustomCreateDTO
                .builder()
                .cardName(dto.getCardName())
                .cardImgFileName(dto.getCardImageName())
                .cardPassword("1234")
                .build());
        // 박준우: 커스텀 카드 발급 이벤트 기록
        eventService.recordMissionProgress(dto.getUserId(), "CARD");

        return customCardId;
    }

    private void upload(int customCardId, MultipartFile part) {

        try {
            String fileName =  UploadFiles.uploadAndGetFileName(UploadPathName.getCustomCardPath(), part);
                CardImageVO cardImageVO = CardImageVO.of(fileName, customCardId);
            customCardMapper.createCustomCardImage(cardImageVO);
        } catch (IOException e) {
            throw new RuntimeException(e); // @Transactional에서 감지, 자동 rollback
        }
    }

    @Override
    public CustomCardSaveRequestDTO loadCard(int userId , int customCardId) {

        CustomCardVO customCardVO = customCardMapper.getCard(userId , customCardId);
        List<TextVO> textList =   customCardMapper.getTexts(customCardId);
        List<EmojiVO> emojiVOList =   customCardMapper.getEmojis(customCardId);


        var response = CustomCardSaveRequestDTO.of(customCardVO);

        if(emojiVOList != null && !emojiVOList.isEmpty() ){
            response.setEmojis(emojiVOList.stream().map(x->
                    EmojiDTO.builder()
                            .id(x.getEmojiId())
                            .x(x.getXPos())
                            .y(x.getYPos())
                            .rotation(x.getRotation())
                            .emojiObj(EmojiObjDTO.builder().emoji(x.getEmojiUrl()).build())
                            .emojiType(x.getEmojiType())
                            .build()
            ).collect(Collectors.toList()));


        }

        if(textList != null && !textList.isEmpty() ){

            response.setTexts(textList.stream().map(x->
                    TextDTO.builder().
                            id(x.getTextId()).
                            text(x.getContent()).
                            x(x.getXPos()).
                            y(x.getYPos()).
                            rotation(x.getRotation()).
                            font(x.getFontFamily()).
                            color(x.getFontColor()).
                            size(x.getFontSize()).
                            isBold(x.getIsBold()).
                            build()

            ).collect(Collectors.toList()));

        }

       CardImageVO vo = customCardMapper.getCustomCardImage(customCardId);

        response.setCardName(vo.getCustomCardImageName());

        return response;
    }


}
