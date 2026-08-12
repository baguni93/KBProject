package org.scoula.cardpayment.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import org.scoula.cardpayment.mapper.CardPaymentMapper;
import org.scoula.common.util.UploadPathName;

@Log4j2
@Component
@RequiredArgsConstructor
public class KbCardCatalogScraper implements InitializingBean {

    private final KbCardCatalogRepository catalogRepository;
    private final CardPaymentMapper cardPaymentMapper;
    private static final String TARGET_MOBILE_URL = "https://m.kbcard.com/CRD/DVIEW/MCAM0101";

    private String getSaveDirectory() {
        return UploadPathName.getCardPath();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("==================================================");
        log.info("[카탈로그 매니저] 모바일 웹 정밀 스크래핑 및 카드명 매핑 시작");
        log.info("==================================================");
        scrapeCardCatalogFromMobileWeb();
    }

    public void scrapeCardCatalogFromMobileWeb() {
        try {
            Document doc = Jsoup.connect(TARGET_MOBILE_URL)
                    .userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148")
                    .referrer("https://m.kbcard.com/")
                    .timeout(10000)
                    .get();

            // 부모/자식 중복 선택 방지: 가장 확실한 카드 리스트 단위(li)만 선택
            Elements cardItems = doc.select("ul li, div.card_list_item, div[class*=goods_item]");

            int successCount = 0;
            for (Element item : cardItems) {
                Element imgEl = item.select("img").first();
                if (imgEl == null) {
                    continue;
                }

                String imgUrl = imgEl.attr("src");
                if (imgUrl == null || imgUrl.isEmpty()) {
                    imgUrl = imgEl.attr("data-src");
                }

                if (imgUrl != null && !imgUrl.isEmpty()) {
                    if (imgUrl.startsWith("//")) {
                        imgUrl = "https:" + imgUrl;
                    } else if (imgUrl.startsWith("/")) {
                        imgUrl = "https://m.kbcard.com" + imgUrl;
                    }

                    if (imgUrl.contains("crd") || imgUrl.contains("card") || imgUrl.contains("img")) {
                        String savedFileName = downloadAndSaveImage(imgUrl);
                        if (savedFileName != null) {
                            // 카드 1개 단위 내에서 정밀 카드명 추출
                            String cardName = resolveCardNameFromFile(savedFileName, item);

                            if (cardName != null && !cardName.trim().isEmpty()) {
                                String cleanName = cardName.trim();
                                catalogRepository.putCardInfo(cleanName, savedFileName);

                                // 마스터 테이블 kb_card_product_tbl 에 정보 저장
                                String cardType = cleanName.contains("체크") ? "CHECK" : "CREDIT";
                                try {
                                    cardPaymentMapper.insertOrUpdateCardProduct(cleanName, cardType, savedFileName, 0);
                                } catch (Exception e) {
                                    log.warn("[카탈로그 매니저] DB 마스터 테이블 저장 경고: {}", e.getMessage());
                                }

                                successCount++;
                            }
                        }
                    }
                }
            }

            if (successCount == 0) {
                log.warn("[카탈로그 매니저] 스크래핑 건수 없음 -> 기본 시드 카탈로그 적용");
                loadFallbackSeed();
            } else {
                log.info("[카탈로그 매니저] 모바일 웹 크롤링 완료: 총 {}건 수집됨", successCount);

                log.info("==================================================");
                log.info("📌 [수집된 카드 & 이미지 정밀 매핑 목록]");
                for (Map.Entry<String, String> entry : catalogRepository.getAllCatalog().entrySet()) {
                    log.info("   카드명: [{}] ==> 파일명: [{}]", entry.getKey(), entry.getValue());
                }
                log.info("==================================================");
            }

        } catch (Exception e) {
            log.error("[카탈로그 매니저] 모바일 스크래핑 예외 발생: {}", e.getMessage());
            loadFallbackSeed();
        }
    }

    /**
     * UI 순위 텍스트 배제 및 개별 카드 요소(Element) 내부에서 정확한 카드명 정제
     */
    private String resolveCardNameFromFile(String fileName, Element item) {
        // 1. 해당 카드 아이템 내부의 alt 속성 우선 검사
        Element imgEl = item.select("img").first();
        if (imgEl != null) {
            String alt = imgEl.attr("alt");
            if (isValidCardName(alt)) {
                return cleanCardNameText(alt);
            }
        }

        // 2. 이미지 alt가 비어있는 경우, 카드 아이템 내부의 텍스트 요소 추출
        Element titleEl = item.select(".card_name, .name, .title, strong, dt").first();
        if (titleEl != null) {
            String textName = titleEl.text();
            if (isValidCardName(textName)) {
                return cleanCardNameText(textName);
            }
        }

        // 3. 파일명 기반 하드코딩 매핑 테이블
        if (fileName.contains("00218")) return "KB국민 TBX 카드";
        if (fileName.contains("00236")) return "KB국민 VOLT UP EV 카드";
        if (fileName.contains("01570")) return "KB국민 So Young 체크카드";
        if (fileName.contains("01574")) return "KB국민 체크카드 (그래피티 디자인)";
        if (fileName.contains("01664")) return "KB국민 nori(노리) 체크카드";
        if (fileName.contains("01690")) return "KB국민 직장인보너스 체크카드";
        if (fileName.contains("01914")) return "KB국민 첵첵 체크카드";
        if (fileName.contains("01998")) return "KB국민 가온 올포인트 체크카드";
        if (fileName.contains("02083")) return "LG헬로비전 KB국민카드 II";
        if (fileName.contains("02219")) return "두산베어스 KB국민카드";
        if (fileName.contains("04124")) return "KB Youth Club 체크카드";
        if (fileName.contains("04241")) return "Liiv M Ⅱ 카드";
        if (fileName.contains("04285")) return "스카이패스 티타늄 카드";
        if (fileName.contains("04288")) return "T-economy KB국민카드";
        if (fileName.contains("04366")) return "SK 7mobile Ⅱ 카드";
        if (fileName.contains("07964")) return "가온플래티늄카드";
        if (fileName.contains("07986")) return "노리2 체크카드 (Play)";
        if (fileName.contains("07998")) return "노리2 체크카드 (Global)";
        if (fileName.contains("09106")) return "KB국민 다담카드";
        if (fileName.contains("09123")) return "KB국민 청춘대로 톡톡카드";
        if (fileName.contains("09125")) return "KB국민 탄탄대로 온리유 카드";
        if (fileName.contains("09126")) return "KB국민 청춘대로 카드";
        if (fileName.contains("09127")) return "KB국민 이지픽(Easy Pick) 카드";
        if (fileName.contains("09128")) return "KB국민 알파원(Alpha One) 카드";
        if (fileName.contains("09129")) return "KB국민 탄탄대로 올쇼핑 카드";
        if (fileName.contains("09137")) return "KB국민 마이 위시(My WE:SH) 카드";
        if (fileName.contains("09138")) return "KB국민 위시 올(WE:SH All) 카드";
        if (fileName.contains("09139")) return "KB국민 위시 디어(WE:SH Dear) 카드";
        if (fileName.contains("09152")) return "KB국민 탄탄대로 Biz 카드";
        if (fileName.contains("09162")) return "KB국민 청춘대로 티타늄 카드";
        if (fileName.contains("09292")) return "KB국민 이지온(Easy On) 카드";
        if (fileName.contains("09297")) return "KB국민 이지홈(Easy Home) 카드";
        if (fileName.contains("09298")) return "KB국민 이지스마트(Easy Smart) 카드";
        if (fileName.contains("09305")) return "KB국민 나라사랑카드";
        if (fileName.contains("09306")) return "KB국민 가온 파이낸스 카드";
        if (fileName.contains("09310")) return "KB국민 쇼핑앤쇼핑 카드";
        if (fileName.contains("09322")) return "KB국민 와이즈홈 카드";
        if (fileName.contains("09348")) return "KB국민 와이즈오토 카드";
        if (fileName.contains("09561")) return "KB국민 가온누리 카드";
        if (fileName.contains("09563")) return "KB국민 가온누리 체크카드";
        if (fileName.contains("09570")) return "KB국민 가온누리 쇼핑 카드";
        if (fileName.contains("09659")) return "KB국민 가온누리 비즈 카드";
        if (fileName.contains("09701")) return "KB국민 가온누리 플러스 카드";
        if (fileName.contains("09771")) return "KB국민 가온누리 트래블 카드";
        if (fileName.contains("09780")) return "KB국민 가온누리 스마트 카드";
        if (fileName.contains("79562")) return "KB국민 나라사랑체크카드";
        if (fileName.contains("09790")) return "KB국민 청춘대로 싱글 체크카드";
        if (fileName.contains("09792")) return "KB국민 청춘대로 오일 체크카드";
        if (fileName.contains("09800")) return "KB국민 청춘대로 톡톡 체크카드";
        if (fileName.contains("09821")) return "KB국민 청춘대로 아임인 체크카드";
        if (fileName.contains("09922")) return "KB국민 청춘대로 프리미엄 체크카드";
        if (fileName.contains("09924")) return "KB국민 청춘대로 티타늄 체크카드";
        if (fileName.contains("19565")) return "KB국민 나라사랑카드";

        return "KB국민카드 상품 (" + fileName.replace(".png", "").replace(".jpg", "") + ")";
    }

    private boolean isValidCardName(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        String t = text.trim();
        // UI용 불필요 단어 필터링 ('연회비' 추가)
        return !t.contains("위") && !t.contains("발급") && !t.contains("정지")
                && !t.contains("이미지") && !t.contains("연회비") && t.length() > 1;
    }

    private String cleanCardNameText(String text) {
        return text.replaceAll("\\[.*?\\]", "") // [혜택] 같은 태그 제거
                .replaceAll("\\s+", " ")     // 연속 공백 하나로 축소
                .trim();
    }

    private String downloadAndSaveImage(String imgUrl) {
        try {
            File dir = new File(getSaveDirectory());
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = imgUrl.substring(imgUrl.lastIndexOf('/') + 1);
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf('?'));
            }
            if (!fileName.toLowerCase().endsWith(".png") && !fileName.toLowerCase().endsWith(".jpg")) {
                fileName = fileName + ".png";
            }

            File targetFile = new File(dir, fileName);
            if (targetFile.exists() && targetFile.length() > 2000) {
                return fileName;
            }

            URL url = new URL(imgUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15");
            conn.setRequestProperty("Referer", "https://m.kbcard.com/");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (InputStream in = conn.getInputStream()) {
                    Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    if (targetFile.length() < 2000) {
                        targetFile.delete();
                        return null;
                    }
                    return fileName;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void loadFallbackSeed() {
        catalogRepository.putCardInfo("KB Pay 노리2 체크카드", "09297_img.png");
        catalogRepository.putCardInfo("KB국민 톡톡MyPoint 카드", "09129_img.png");
        log.info("[카탈로그 매니저] 검증된 기본 시드 카드 매핑 완료.");
    }
}