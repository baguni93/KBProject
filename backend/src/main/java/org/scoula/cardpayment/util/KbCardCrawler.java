package org.scoula.cardpayment.util;

import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.scoula.cardpayment.dto.CardBinResponseDTO;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class KbCardCrawler {

    public CardBinResponseDTO crawlCardInfo(String binNumber) {
        try {
            String searchUrl = "https://card.kbcard.com/CRD/DMC/CLI/CCDMMC0001.cms?mainMnuKOR=" + binNumber;

            Document doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(2500)
                    .get();

            Element imgElement = doc.selectFirst(".card_img img");
            Element nameElement = doc.selectFirst(".card_title");

            String imageUrl = (imgElement != null) ? imgElement.attr("src") : "";
            String cardName = (nameElement != null) ? nameElement.text() : "KB국민 신용/체크카드";

            if (imageUrl.startsWith("/")) {
                imageUrl = "https://img.kbcard.com" + imageUrl;
            }

            return CardBinResponseDTO.builder()
                    .binNumber(binNumber)
                    .cardName(cardName)
                    .imageUrl(imageUrl)
                    .build();

        } catch (Exception e) {
            log.warn("KB국민카드 라이브 크롤링 실패 (BIN: {}): {}", binNumber, e.getMessage());
            return CardBinResponseDTO.builder()
                    .binNumber(binNumber)
                    .cardName("KB국민카드")
                    .imageUrl("")
                    .build();
        }
    }
}