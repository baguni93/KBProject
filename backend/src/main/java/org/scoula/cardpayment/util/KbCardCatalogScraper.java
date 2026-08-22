package org.scoula.cardpayment.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.scoula.cardpayment.dto.CrawledCardBenefitDTO;
import org.scoula.cardpayment.dto.CrawledCardProductDTO;
import org.scoula.cardpayment.mapper.CardPaymentMapper;
import org.scoula.common.util.UploadPathName;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 서버 시작 시 KB국민카드 카탈로그를 수집해 카드 추천용 마스터 데이터를 재구축한다.
 *
 * 처리 순서
 * 1. 신용카드/체크카드 전용 목록을 각각 순회해 카드명, 이미지, 상품코드를 수집한다.
 * 2. 목록의 종류로 CREDIT/CHECK를 확정하고 상품코드 기준으로 중복을 제거한다.
 * 3. 상품코드로 상세 페이지를 조회해 연회비와 현재 계산기가 지원하는 할인 혜택을 추출한다.
 * 4. 전체 수집 결과가 정상일 때만 추천 결과 -> 혜택 -> 상품 순으로 TRUNCATE한다.
 * 5. 상품 INSERT 후 생성된 PK를 사용해 혜택을 INSERT한다.
 *
 * RootConfig에서만 이 패키지를 스캔하므로 애플리케이션 시작당 한 번만 실행된다.
 */
@Component
public class KbCardCatalogScraper implements InitializingBean {

    private static final Logger log =
            LogManager.getLogger(KbCardCatalogScraper.class);

    private static final String KB_CARD_BASE_URL =
            "https://card.kbcard.com";

    private static final String CREDIT_LIST_URL =
            KB_CARD_BASE_URL + "/CRD/DVIEW/HCAMCXPRICAC0047";

    private static final String CHECK_LIST_URL =
            KB_CARD_BASE_URL + "/CRD/DVIEW/HCAMCXPRICAC0056";

    private static final String DETAIL_URL_TEMPLATE =
            "https://card.kbcard.com/CRD/DVIEW/HCAMCXPRICAC0076"
                    + "?cooperationcode=%s&mainCC=a";

    private static final String DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                    + "AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/124.0.0.0 Safari/537.36";

    private static final int MINIMUM_REPLACE_CARD_COUNT_PER_TYPE = 2;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final int LIST_REQUEST_TIMEOUT_MILLIS = 15000;

    /*
     * 같은 상품이 여러 혜택 카테고리에 중복 노출된다. 목록의 종류와
     * 상품코드를 조합해 중복을 제거하며, 카드명으로 종류를 추측하지 않는다.
     */
    private static final List<CardListSource> CARD_LIST_SOURCES = List.of(
            new CardListSource("CREDIT", 1),
            new CardListSource("CREDIT", 5),
            new CardListSource("CREDIT", 2),
            new CardListSource("CREDIT", 3),
            new CardListSource("CREDIT", 10),
            new CardListSource("CREDIT", 8),
            new CardListSource("CREDIT", 9),
            new CardListSource("CREDIT", 6),
            new CardListSource("CREDIT", 12),
            new CardListSource("CHECK", 12),
            new CardListSource("CHECK", 13),
            new CardListSource("CHECK", 14),
            new CardListSource("CHECK", 15),
            new CardListSource("CHECK", 16),
            new CardListSource("CHECK", 17),
            new CardListSource("CHECK", 10),
            new CardListSource("CHECK", 18)
    );

    private static final Pattern PRODUCT_CODE_PATTERN =
            Pattern.compile("(?<!\\d)(\\d{5})(?!\\d)");

    private static final Pattern COOPERATION_CODE_PATTERN =
            Pattern.compile("cooperationcode(?:=|%3D)(\\d{5})",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern GO_DETAIL_CODE_PATTERN =
            Pattern.compile("goDetail\\s*\\(\\s*['\"](\\d{5})['\"]",
                    Pattern.CASE_INSENSITIVE);

    /*
     * 페이지 안의 가장 큰 퍼센트를 고르지 않고, 실제로 "% 할인"과
     * 직접 연결된 숫자만 혜택 할인율로 인정한다.
     */
    private static final Pattern DISCOUNT_RATE_PATTERN =
            Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*%\\s*"
                    + "(?:(?:청구|환급)(?:\\s*/\\s*(?:청구|환급))?\\s*)?할인");

    private static final Pattern FIXED_DISCOUNT_PATTERN = Pattern.compile(
            "((?:\\d[\\d,]*\\s*만\\s*(?:\\d[\\d,]*\\s*천)?\\s*원)"
                    + "|(?:\\d[\\d,]*\\s*천\\s*\\d[\\d,]*\\s*백\\s*원)"
                    + "|(?:\\d[\\d,]*\\s*(?:만원|천원|원)))"
                    + "\\s*(?:(?:청구|환급)"
                    + "(?:\\s*/\\s*(?:청구|환급))?\\s*)?할인"
    );

    private static final Pattern MONEY_RANGE_PATTERN = Pattern.compile(
            "\\d[\\d,]*\\s*[~～-]\\s*\\d[\\d,]*\\s*(?:만원|천원|원)"
    );

    private static final Pattern MAXIMUM_RATE_PATTERN =
            Pattern.compile("최대\\s*\\d+(?:\\.\\d+)?\\s*%");

    private static final Pattern COMPLEX_MONEY_PATTERN =
            Pattern.compile("(\\d[\\d,]*)\\s*만\\s*"
                    + "(?:(\\d[\\d,]*)\\s*천)?\\s*원");

    private static final Pattern THOUSAND_HUNDRED_MONEY_PATTERN =
            Pattern.compile("(\\d[\\d,]*)\\s*천\\s*(\\d[\\d,]*)\\s*백\\s*원");

    private static final Pattern SIMPLE_MONEY_PATTERN =
            Pattern.compile("(\\d[\\d,]*)\\s*(만원|천원|원)");

    private static final Pattern MONTHLY_FREQUENCY_PATTERN =
            Pattern.compile("월\\s*(\\d+)\\s*회");

    /*
     * 주요혜택 문구 자체에 아래 상호가 표시되면 카테고리 전체 혜택으로
     * 일반화하지 않는다. 상세혜택의 상호명은 조건 설명에 불과하므로 이
     * 필터는 반드시 주요혜택 문구에만 적용한다.
     */
    private static final List<String> MERCHANT_KEYWORDS = List.of(
            "스타벅스", "커피빈", "CGV", "아웃백", "VIPS",
            "에버랜드", "롯데월드", "GS25", "CU", "교보문고",
            "YES24", "올리브영", "인터파크", "구글플레이스토어",
            "앱스토어", "넷플릭스", "유튜브", "티빙", "웨이브",
            "디즈니 플러스", "배달의민족", "요기요", "G마켓",
            "옥션", "11번가", "쿠팡", "이마트", "롯데마트",
            "홈플러스", "SSG", "네이버페이", "카카오페이"
    );

    private static final Set<String> BENEFIT_MATCH_STOP_WORDS = Set.of(
            "할인", "청구할인", "환급할인", "서비스", "주요혜택",
            "업종", "이용", "이용금액", "제공", "카드", "월",
            "이상", "이내", "최대", "기준", "자동납부"
    );

    private final KbCardCatalogRepository catalogRepository;
    private final CardPaymentMapper cardPaymentMapper;
    private final DataSource dataSource;

    public KbCardCatalogScraper(
            KbCardCatalogRepository catalogRepository,
            CardPaymentMapper cardPaymentMapper,
            DataSource dataSource
    ) {
        this.catalogRepository = catalogRepository;
        this.cardPaymentMapper = cardPaymentMapper;
        this.dataSource = dataSource;
    }

    private String getSaveDirectory() {
        return UploadPathName.getCardPath();
    }

    // 카드 크롤링(앱 시연시에 반드시 다시 켜야한다.)
    @Override
    public void afterPropertiesSet() {
        log.info("==================================================");
        log.info("[카탈로그 매니저] 카드 추천용 상품/혜택 자동 동기화 시작");
        log.info("==================================================");
        scrapeCardCatalogFromKbWeb();
    }

    /**
     * 네트워크 수집이 완전히 끝난 뒤에만 DB를 비운다.
     * KB카드 접속 실패나 HTML 구조 변경 시 기존 DB 데이터는 유지된다.
     */
    public void scrapeCardCatalogFromKbWeb() {
        try {
            List<CrawledCardProductDTO> products = crawlAllProducts();

            if (!hasMinimumProductCounts(products)) {
                log.warn("[카탈로그 매니저] 카드 종류별 최소 수집 건수를 충족하지 못해 "
                                + "DB 교체를 생략합니다. 전체 {}건",
                        products.size());
                loadFallbackSeed();
                return;
            }

            if (!isSafeRecommendationCatalog(products)) {
                log.error("[카탈로그 매니저] 비정상 혜택이 감지되어 DB 교체를 생략합니다.");
                loadFallbackSeed();
                return;
            }

            replaceRecommendationCatalog(products);
            logCollectedCatalog(products);
        } catch (Exception e) {
            log.error("[카탈로그 매니저] 카드 카탈로그 동기화 예외: {}",
                    e.getMessage(), e);
            loadFallbackSeed();
        }
    }

    private List<CrawledCardProductDTO> crawlAllProducts() throws Exception {
        Map<String, ListCardSeed> seedsByProductKey = new LinkedHashMap<>();

        for (CardListSource source : CARD_LIST_SOURCES) {
            crawlListSource(source, seedsByProductKey);
        }

        List<CrawledCardProductDTO> products = new ArrayList<>();
        for (ListCardSeed seed : seedsByProductKey.values()) {
            String savedFileName = downloadAndSaveImage(seed.imageUrl);
            if (savedFileName == null || savedFileName.contains("logo-")) {
                log.warn("[카탈로그 매니저] 이미지 저장 실패로 상품 제외: "
                                + "type={}, code={}, card={}",
                        seed.cardType, seed.productCode, seed.cardName);
                continue;
            }

            String detailUrl = String.format(
                    DETAIL_URL_TEMPLATE,
                    seed.productCode
            );
            products.add(crawlProductDetails(
                    seed.cardName,
                    seed.summary,
                    savedFileName,
                    detailUrl,
                    seed.cardType,
                    seed.listPageUrl
            ));
        }

        long creditCount = products.stream()
                .filter(product -> "CREDIT".equals(product.getCardType()))
                .count();
        long checkCount = products.stream()
                .filter(product -> "CHECK".equals(product.getCardType()))
                .count();
        log.info("[카탈로그 매니저] 목록 수집 결과: 신용카드 {}건, 체크카드 {}건, 전체 {}건",
                creditCount, checkCount, products.size());
        return products;
    }

    private void crawlListSource(
            CardListSource source,
            Map<String, ListCardSeed> seedsByProductKey
    ) throws Exception {
        String pageUrl = source.buildPageUrl();
        Document listDocument = Jsoup.connect(pageUrl)
                .userAgent(DESKTOP_USER_AGENT)
                .referrer(KB_CARD_BASE_URL + "/")
                .timeout(LIST_REQUEST_TIMEOUT_MILLIS)
                .followRedirects(true)
                .get();

        Elements cardItems = listDocument.select("div.card-box__item");
        if (cardItems.isEmpty()) {
            throw new IllegalStateException(
                    "카드 목록 HTML에서 상품 영역을 찾지 못했습니다: " + pageUrl
            );
        }

        Set<String> pageProductCodes = new TreeSet<>();
        int newProductCount = 0;
        for (Element item : cardItems) {
            ListCardSeed seed = parseListCardSeed(item, source, pageUrl);
            if (seed == null) {
                continue;
            }

            pageProductCodes.add(seed.productCode);
            String key = seed.cardType + ":" + seed.productCode;
            ListCardSeed existing = seedsByProductKey.get(key);
            if (existing == null) {
                seedsByProductKey.put(key, seed);
                newProductCount++;
            } else {
                existing.mergeMissingFields(seed);
            }
        }

        if (pageProductCodes.isEmpty()) {
            throw new IllegalStateException(
                    "카드 목록 HTML에서 상품코드를 읽지 못했습니다: " + pageUrl
            );
        }

        log.info("[카탈로그 매니저] 목록 페이지 수집: "
                        + "type={}, cateIdx={}, 노출 {}건, 신규 {}건",
                source.cardType, source.categoryIndex,
                pageProductCodes.size(), newProductCount);
    }

    private boolean hasMinimumProductCounts(
            List<CrawledCardProductDTO> products
    ) {
        long creditCount = products.stream()
                .filter(product -> "CREDIT".equals(product.getCardType()))
                .count();
        long checkCount = products.stream()
                .filter(product -> "CHECK".equals(product.getCardType()))
                .count();
        return creditCount >= MINIMUM_REPLACE_CARD_COUNT_PER_TYPE
                && checkCount >= MINIMUM_REPLACE_CARD_COUNT_PER_TYPE;
    }

    private ListCardSeed parseListCardSeed(
            Element item,
            CardListSource source,
            String pageUrl
    ) {
        String productCode = extractProductCode(item);
        if (productCode == null) {
            return null;
        }

        String cardName = resolveListCardName(item);
        if (!isValidCardName(cardName)) {
            return null;
        }

        Element imageElement = item.selectFirst("img");
        if (imageElement == null) {
            return null;
        }

        String imageUrl = resolveImageUrl(imageElement);
        if (!isLikelyCardImage(imageUrl)) {
            return null;
        }

        return new ListCardSeed(
                source.cardType,
                productCode,
                cleanCardNameText(cardName),
                resolveListSummary(item),
                imageUrl,
                pageUrl
        );
    }

    private String extractProductCode(Element item) {
        Matcher detailMatcher = GO_DETAIL_CODE_PATTERN.matcher(item.html());
        if (detailMatcher.find()) {
            return detailMatcher.group(1);
        }

        Matcher cooperationMatcher = COOPERATION_CODE_PATTERN.matcher(item.html());
        if (cooperationMatcher.find()) {
            return cooperationMatcher.group(1);
        }

        Element image = item.selectFirst("img");
        if (image != null) {
            Matcher imageMatcher = PRODUCT_CODE_PATTERN.matcher(resolveImageUrl(image));
            if (imageMatcher.find()) {
                return imageMatcher.group(1);
            }
        }
        return null;
    }

    private String resolveListCardName(Element item) {
        Element heading = item.selectFirst("h3.tit-dep4, h3, .card_name, .name, .title");
        if (heading != null && isValidCardName(heading.text())) {
            return heading.text();
        }

        Element image = item.selectFirst("img[alt]");
        return image == null ? null : image.attr("alt");
    }

    private String resolveListSummary(Element item) {
        for (Element summary : item.select("p.badge--txt")) {
            String text = normalizeText(summary.text());
            if (!text.isBlank()) {
                return truncate(text, MAX_DESCRIPTION_LENGTH);
            }
        }
        return null;
    }

    private CrawledCardProductDTO crawlProductDetails(
            String cardName,
            String summary,
            String savedFileName,
            String detailUrl,
            String cardType,
            String listPageUrl
    ) {
        CrawledCardProductDTO product = new CrawledCardProductDTO();
        product.setCardName(cardName);
        product.setCardType(cardType);
        product.setCardDescription(truncate(summary, MAX_DESCRIPTION_LENGTH));
        product.setCardImage(savedFileName);
        product.setApplication(detailUrl);
        product.setAnnualFee(0);
        product.setBenefits(new ArrayList<>());

        if (detailUrl == null || detailUrl.isBlank()) {
            return product;
        }

        try {
            Document detailDocument = Jsoup.connect(detailUrl)
                    .userAgent(DESKTOP_USER_AGENT)
                    .referrer(listPageUrl)
                    .timeout(LIST_REQUEST_TIMEOUT_MILLIS)
                    .followRedirects(true)
                    .get();

            Element productContainer = findProductContainer(detailDocument, cardName);
            String productText = normalizeText(productContainer.text());

            String detailCardName = resolveDetailCardName(productContainer, cardName);
            if (isValidCardName(detailCardName)) {
                product.setCardName(cleanCardNameText(detailCardName));
            }

            product.setAnnualFee(extractAnnualFee(productText));
            product.setCardDescription(resolveProductDescription(summary, productContainer));
            product.setBenefits(parseMainBenefits(productContainer));

        } catch (Exception e) {
            log.warn("[카탈로그 매니저] 상세 페이지 수집 실패, 목록 정보 사용: card={}, reason={}",
                    cardName, e.getMessage());
        }

        return product;
    }

    private boolean isSafeRecommendationCatalog(
            List<CrawledCardProductDTO> products
    ) {
        for (CrawledCardProductDTO product : products) {
            Set<String> categories = new HashSet<>();
            List<CrawledCardBenefitDTO> productBenefits =
                    product.getBenefits() == null
                            ? List.of()
                            : product.getBenefits();

            for (CrawledCardBenefitDTO benefit : productBenefits) {
                if (benefit == null
                        || benefit.getCategoryName() == null
                        || !categories.add(benefit.getCategoryName())) {
                    return false;
                }

                BigDecimal rate = benefit.getBenefitRate();
                Integer amount = benefit.getBenefitAmount();
                if ((rate == null) == (amount == null)) {
                    return false;
                }
                if (rate != null && rate.compareTo(BigDecimal.ZERO) <= 0) {
                    return false;
                }
                if (amount != null && amount <= 0) {
                    return false;
                }
                if (benefit.getMonthlyLimit() != null
                        && benefit.getMonthlyLimit() <= 0) {
                    return false;
                }
                if (benefit.getMinimumSpendingAmount() != null
                        && benefit.getMinimumSpendingAmount() < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 추천 결과와 상세 결과도 새 상품/혜택 PK를 참조하므로 함께 초기화한다.
     * MySQL TRUNCATE는 FK가 정의된 부모 테이블에서 거부되므로 동일 Connection에서
     * FK 검사를 잠시 끈 뒤 자식 -> 부모 순서로 처리한다.
     */
    private void truncateRecommendationTables() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            try {
                statement.executeUpdate("TRUNCATE TABLE card_recommendation_detail_tbl");
                statement.executeUpdate("TRUNCATE TABLE card_recommendation_tbl");
                statement.executeUpdate("TRUNCATE TABLE card_benefit_tbl");
                statement.executeUpdate("TRUNCATE TABLE kb_card_product_tbl");
            } finally {
                statement.execute("SET FOREIGN_KEY_CHECKS = 1");
            }
        }
    }

    private void replaceRecommendationCatalog(
            List<CrawledCardProductDTO> products
    ) throws Exception {
        // 네트워크 수집과 파싱이 끝난 뒤에만 기존 추천 카탈로그를 비운다.
        truncateRecommendationTables();
        catalogRepository.clear();

        Map<String, Integer> categoryIdCache = new LinkedHashMap<>();
        int insertedProducts = 0;
        int insertedBenefits = 0;

        for (CrawledCardProductDTO product : products) {
            product.setCardProductId(null);
            int productRows = cardPaymentMapper.insertCrawledCardProduct(product);

            if (productRows != 1 || product.getCardProductId() == null) {
                throw new IllegalStateException(
                        "카드 상품 INSERT 실패: " + product.getCardName()
                );
            }

            insertedProducts++;
            catalogRepository.putCardInfo(
                    product.getCardName(),
                    product.getCardImage()
            );

            for (CrawledCardBenefitDTO benefit : product.getBenefits()) {
                Integer categoryId = categoryIdCache.computeIfAbsent(
                        benefit.getCategoryName(),
                        cardPaymentMapper::findSpendingCategoryIdByName
                );

                if (categoryId == null) {
                    log.warn("[카탈로그 매니저] 카테고리 미등록으로 혜택 제외: card={}, category={}",
                            product.getCardName(), benefit.getCategoryName());
                    continue;
                }

                benefit.setCardProductId(product.getCardProductId());
                benefit.setSpendingCategoryId(categoryId);
                insertedBenefits += cardPaymentMapper.insertCrawledCardBenefit(benefit);
            }
        }

        log.info("[카탈로그 매니저] DB 전체 교체 완료: 상품 {}건, 혜택 {}건",
                insertedProducts, insertedBenefits);
    }

    /**
     * 혜택 행은 반드시 "주요혜택" 영역에서만 만든다.
     * "상세혜택"은 주요혜택과 매칭해 전월실적, 월 할인한도, 설명을
     * 보충하는 용도로만 사용한다.
     */
    private List<CrawledCardBenefitDTO> parseMainBenefits(Element container) {
        Element mainHeading = findExactSectionHeading(container, "주요혜택");
        if (mainHeading == null) {
            log.warn("[카탈로그 매니저] 주요혜택 영역을 찾지 못했습니다.");
            return List.of();
        }

        List<Element> mainItems = collectMainBenefitItems(container, mainHeading);
        List<BenefitDetailSection> detailSections = collectDetailSections(container);
        List<CrawledCardBenefitDTO> benefits = new ArrayList<>();
        Set<String> processedTexts = new LinkedHashSet<>();

        for (Element item : mainItems) {
            String mainText = normalizeText(item.text());
            if (!processedTexts.add(mainText)
                    || !isSupportedBenefitText(mainText)
                    || hasUnrepresentableCondition(mainText)
                    || isMerchantSpecificMainBenefit(mainText)) {
                continue;
            }

            BigDecimal rate = extractDiscountRate(mainText);
            Integer amount = rate == null
                    ? extractFixedDiscountAmount(mainText)
                    : null;
            if (rate == null && amount == null) {
                continue;
            }

            List<String> categoryNames = resolveCategoryNames(mainText);
            if (categoryNames.isEmpty()) {
                continue;
            }

            for (String categoryName : categoryNames) {
                BenefitDetailSection detail = findMatchingDetailSection(
                        mainText,
                        categoryName,
                        detailSections
                );
                String detailText = narrowDetailText(
                        mainText,
                        categoryName,
                        detail
                );
                BenefitDetailSection narrowedDetail = detail == null
                        ? null
                        : new BenefitDetailSection(
                        detail.heading,
                        detailText,
                        detail.tableRows
                );
                String conditionText = normalizeText(mainText + " " + detailText);

                Integer minimumSpending = extractMinimumSpending(conditionText);
                if (minimumSpending == null && detail != null
                        && !hasExplicitNoMinimumSpending(conditionText)) {
                    minimumSpending = extractMinimumSpending(detail.text);
                }
                Integer monthlyLimit = resolveMonthlyLimit(
                        mainText,
                        narrowedDetail,
                        minimumSpending,
                        amount
                );

                boolean noMinimum = hasExplicitNoMinimumSpending(conditionText);
                boolean noLimit = hasExplicitNoMonthlyLimit(conditionText);

                // 상세혜택 매칭에 실패하거나 조건을 특정할 수 없으면 과대 계산을
                // 막기 위해 그 주요혜택만 제외한다. 원문에 조건/한도 없음이
                // 명시된 경우에만 NULL 저장을 허용한다.
                if ((minimumSpending == null && !noMinimum)
                        || (monthlyLimit == null && !noLimit)) {
                    log.debug("[카탈로그 매니저] 상세 조건 미확정으로 혜택 제외: "
                                    + "main={}, category={}, detail={}",
                            mainText,
                            categoryName,
                            detail == null ? "미매칭" : detail.heading);
                    continue;
                }

                benefits.add(createCrawledBenefit(
                        categoryName,
                        rate,
                        amount,
                        monthlyLimit,
                        minimumSpending
                ));
            }
        }

        return deduplicateBenefits(benefits);
    }

    private CrawledCardBenefitDTO createCrawledBenefit(
            String categoryName,
            BigDecimal rate,
            Integer amount,
            Integer monthlyLimit,
            Integer minimumSpending
    ) {
        String benefitName = resolveBenefitName(categoryName);
        String benefitDescription = buildBenefitDescription(
                categoryName,
                rate,
                amount
        );

        CrawledCardBenefitDTO benefit = new CrawledCardBenefitDTO();
        benefit.setCategoryName(categoryName);
        benefit.setBenefitName(truncate(benefitName, 150));
        benefit.setBenefitAmount(amount);
        benefit.setBenefitRate(rate);
        benefit.setMonthlyLimit(monthlyLimit);
        benefit.setMinimumSpendingAmount(minimumSpending);
        benefit.setBenefitDescription(benefitDescription);
        return benefit;
    }

    /**
     * 화면에는 KB 페이지의 긴 상세 원문을 저장하지 않고,
     * 이미 추출한 카테고리와 할인값으로 1줄 설명을 생성한다.
     */
    private String buildBenefitDescription(
            String categoryName,
            BigDecimal rate,
            Integer amount
    ) {
        String target = resolveBenefitTargetName(categoryName);
        if (rate != null) {
            return target + " 이용 시 "
                    + rate.stripTrailingZeros().toPlainString()
                    + "% 할인 혜택을 제공합니다.";
        }
        return target + " 이용 시 "
                + String.format(Locale.ROOT, "%,d", amount)
                + "원 할인 혜택을 제공합니다.";
    }

    private String resolveBenefitName(String categoryName) {
        switch (categoryName) {
            case "식비":
                return "음식점 할인";
            case "카페":
                return "카페 이용 할인";
            case "생활":
                return "생활 업종 할인";
            case "온라인쇼핑":
                return "온라인 쇼핑 할인";
            case "뷰티/미용":
                return "뷰티·미용 할인";
            case "교통":
                return "대중교통 할인";
            case "자동차":
                return "자동차 관련 할인";
            case "주거/통신":
                return "주거·통신 할인";
            case "금융":
                return "금융 서비스 할인";
            case "여행":
                return "여행 할인";
            case "교육":
                return "교육 할인";
            case "병원":
                return "병원 이용 할인";
            case "반려동물":
                return "반려동물 할인";
            default:
                return categoryName + " 할인";
        }
    }

    private String resolveBenefitTargetName(String categoryName) {
        switch (categoryName) {
            case "식비":
                return "음식점";
            case "카페":
                return "카페";
            case "생활":
                return "생활 업종";
            case "온라인쇼핑":
                return "온라인 쇼핑";
            case "뷰티/미용":
                return "뷰티·미용 업종";
            case "교통":
                return "대중교통";
            case "자동차":
                return "자동차 관련 업종";
            case "주거/통신":
                return "주거·통신 요금";
            case "금융":
                return "금융 서비스";
            case "여행":
                return "여행 업종";
            case "교육":
                return "교육 업종";
            case "병원":
                return "병원";
            case "반려동물":
                return "반려동물 업종";
            default:
                return categoryName;
        }
    }

    private Element findExactSectionHeading(Element container, String title) {
        for (Element heading : container.select("h1, h2, h3, h4, h5, h6")) {
            if (title.equals(normalizeText(heading.text()))) {
                return heading;
            }
        }
        return null;
    }

    private List<Element> collectMainBenefitItems(
            Element container,
            Element mainHeading
    ) {
        List<Element> sectionElements = collectElementsUntilNextSection(
                container,
                mainHeading
        );
        List<Element> result = new ArrayList<>();
        Set<String> texts = new LinkedHashSet<>();

        // KB 상품 페이지의 주요혜택 카드는 보통 li로 구성된다. 일부 구형
        // 페이지를 위해 dt, tr도 허용한다.
        for (Element element : sectionElements) {
            if (!hasTag(element, "li", "dt", "tr")) {
                continue;
            }
            String text = normalizeText(element.text());
            if (isSupportedBenefitText(text)
                    && texts.add(text)) {
                result.add(element);
            }
        }

        // 레거시 페이지가 div/p로만 구성된 경우의 제한적인 보조 처리다.
        if (result.isEmpty()) {
            for (Element element : sectionElements) {
                if (!hasTag(element, "p", "div")) {
                    continue;
                }
                String text = normalizeText(element.text());
                if (isSupportedBenefitText(text)
                        && texts.add(text)
                        && !hasNestedBenefitItem(element)) {
                    result.add(element);
                }
            }
        }
        return result;
    }

    private boolean hasNestedBenefitItem(Element element) {
        for (Element child : element.children()) {
            if (!hasTag(child, "li", "dt", "tr", "p", "div")) {
                continue;
            }
            String childText = normalizeText(child.text());
            if (isSupportedBenefitText(childText)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTag(Element element, String... tagNames) {
        for (String tagName : tagNames) {
            if (tagName.equalsIgnoreCase(element.tagName())) {
                return true;
            }
        }
        return false;
    }

    private List<Element> collectElementsUntilNextSection(
            Element container,
            Element startHeading
    ) {
        List<Element> all = container.getAllElements();
        List<Element> result = new ArrayList<>();
        int startIndex = all.indexOf(startHeading);
        int startLevel = headingLevel(startHeading);

        if (startIndex < 0) {
            return result;
        }

        for (int i = startIndex + 1; i < all.size(); i++) {
            Element current = all.get(i);
            int currentLevel = headingLevel(current);
            if (currentLevel > 0 && currentLevel <= startLevel) {
                break;
            }
            result.add(current);
        }
        return result;
    }

    private int headingLevel(Element element) {
        String tag = element.tagName();
        if (tag.length() == 2 && tag.charAt(0) == 'h'
                && Character.isDigit(tag.charAt(1))) {
            return Character.digit(tag.charAt(1), 10);
        }
        return 0;
    }

    private List<BenefitDetailSection> collectDetailSections(Element container) {
        List<BenefitDetailSection> result = new ArrayList<>();
        for (Element heading : container.select("h1, h2, h3, h4, h5, h6")) {
            String headingText = normalizeText(heading.text());
            if (!headingText.startsWith("상세혜택")) {
                continue;
            }

            List<Element> sectionElements = collectElementsUntilNextSection(
                    container,
                    heading
            );
            StringBuilder text = new StringBuilder(headingText);
            List<String> tableRows = new ArrayList<>();

            for (Element element : sectionElements) {
                String ownText = normalizeText(element.ownText());
                if (!ownText.isBlank()) {
                    text.append(' ').append(ownText);
                }
                if ("tr".equals(element.tagName())) {
                    String rowText = normalizeText(element.text());
                    if (!rowText.isBlank()) {
                        tableRows.add(rowText);
                    }
                }
            }

            result.add(new BenefitDetailSection(
                    headingText,
                    truncate(text.toString(), 5000),
                    tableRows
            ));
        }
        return result;
    }

    private BenefitDetailSection findMatchingDetailSection(
            String mainText,
            String categoryName,
            List<BenefitDetailSection> sections
    ) {
        Set<String> tokens = extractBenefitMatchTokens(mainText);
        BenefitDetailSection best = null;
        int bestScore = 0;

        for (BenefitDetailSection section : sections) {
            String heading = section.heading;
            String text = section.text;
            int score = 0;

            for (String token : tokens) {
                if (heading.contains(token)) {
                    score += 12;
                } else if (text.contains(token)) {
                    score += 2;
                }
            }

            for (String alias : categoryAliases(categoryName)) {
                if (heading.contains(alias)) {
                    score += 10;
                }
            }

            // 전용 상세 탭이 있으면 모든 혜택을 포함하는 서비스요약보다 우선한다.
            if (heading.contains("서비스요약") || heading.contains("서비스 요약")) {
                score -= 3;
            }

            if (score > bestScore) {
                bestScore = score;
                best = section;
            }
        }
        return bestScore > 0 ? best : null;
    }

    private String narrowDetailText(
            String mainText,
            String categoryName,
            BenefitDetailSection detail
    ) {
        if (detail == null) {
            return "";
        }

        Set<String> anchors = new LinkedHashSet<>(extractBenefitMatchTokens(mainText));
        anchors.addAll(categoryAliases(categoryName));

        int searchFrom = Math.min(detail.heading.length(), detail.text.length());
        int bestIndex = -1;
        int bestLength = -1;

        // "대중교통/통신"처럼 한 상세 탭에 여러 혜택이 들어 있는 경우,
        // 주요혜택과 가장 구체적으로 일치하는 본문 위치부터 읽어 다른
        // 혜택의 한도를 잘못 가져오지 않게 한다.
        for (String anchor : anchors) {
            int index = detail.text.indexOf(anchor, searchFrom);
            if (index >= 0 && anchor.length() > bestLength) {
                bestIndex = index;
                bestLength = anchor.length();
            }
        }

        if (bestIndex < 0) {
            return detail.text;
        }

        return truncate(
                detail.heading + " " + detail.text.substring(bestIndex),
                3000
        );
    }

    private Set<String> extractBenefitMatchTokens(String text) {
        Set<String> result = new LinkedHashSet<>();
        String withoutMoney = normalizeText(text)
                .replaceAll("\\d+(?:\\.\\d+)?\\s*%", " ")
                .replaceAll("\\d[\\d,]*\\s*(?:만원|천원|원)", " ");

        for (String token : withoutMoney.split("[^가-힣A-Za-z0-9]+")) {
            if (token.length() >= 2
                    && !BENEFIT_MATCH_STOP_WORDS.contains(token)
                    && !token.matches("\\d+")) {
                result.add(token);
            }
        }
        return result;
    }

    private List<String> categoryAliases(String categoryName) {
        switch (categoryName) {
            case "식비":
                return List.of("음식", "외식", "푸드");
            case "카페":
                return List.of("커피", "카페");
            case "생활":
                return List.of("생활", "편의점", "문화", "게임", "PC방");
            case "온라인쇼핑":
                return List.of("온라인", "쇼핑", "오픈마켓");
            case "뷰티/미용":
                return List.of("뷰티", "미용");
            case "교통":
                return List.of("교통", "버스", "지하철", "택시");
            case "자동차":
                return List.of("자동차", "충전", "주차", "세차", "주유");
            case "주거/통신":
                return List.of("관리비", "통신", "모바일");
            case "여행":
                return List.of("여행", "해외", "항공", "숙박");
            case "교육":
                return List.of("교육", "학원", "학습지", "문화센터");
            case "병원":
                return List.of("병원", "의료", "약국", "건강");
            case "반려동물":
                return List.of("반려동물", "펫", "동물병원");
            default:
                return List.of(categoryName);
        }
    }

    private boolean isMerchantSpecificMainBenefit(String mainText) {
        String normalized = normalizeText(mainText).toUpperCase(Locale.ROOT);
        for (String merchant : MERCHANT_KEYWORDS) {
            if (normalized.contains(merchant.toUpperCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static final class BenefitDetailSection {
        private final String heading;
        private final String text;
        private final List<String> tableRows;

        private BenefitDetailSection(
                String heading,
                String text,
                List<String> tableRows
        ) {
            this.heading = heading;
            this.text = text;
            this.tableRows = tableRows;
        }
    }

    /**
     * 카드 종류별 공식 목록 출처. categoryIndex는 KB카드가 사용하는
     * 혜택 카테고리 번호이며, 카드 종류는 이 출처에서 확정한다.
     */
    private static final class CardListSource {
        private final String cardType;
        private final int categoryIndex;

        private CardListSource(String cardType, int categoryIndex) {
            this.cardType = cardType;
            this.categoryIndex = categoryIndex;
        }

        private String buildPageUrl() {
            String listUrl = "CHECK".equals(cardType)
                    ? CHECK_LIST_URL
                    : CREDIT_LIST_URL;
            return listUrl
                    + "?pageNo=1"
                    + "&cateIdx=" + categoryIndex;
        }
    }

    /**
     * 목록에서 먼저 확보하는 최소 상품 정보. 상세 페이지는 동일 상품당
     * 한 번만 조회할 수 있도록 이 단계에서 중복을 제거한다.
     */
    private static final class ListCardSeed {
        private final String cardType;
        private final String productCode;
        private String cardName;
        private String summary;
        private String imageUrl;
        private String listPageUrl;

        private ListCardSeed(
                String cardType,
                String productCode,
                String cardName,
                String summary,
                String imageUrl,
                String listPageUrl
        ) {
            this.cardType = cardType;
            this.productCode = productCode;
            this.cardName = cardName;
            this.summary = summary;
            this.imageUrl = imageUrl;
            this.listPageUrl = listPageUrl;
        }

        private void mergeMissingFields(ListCardSeed incoming) {
            if ((cardName == null || cardName.isBlank())
                    && incoming.cardName != null) {
                cardName = incoming.cardName;
            }
            if ((summary == null || summary.isBlank())
                    && incoming.summary != null) {
                summary = incoming.summary;
            }
            if ((imageUrl == null || imageUrl.isBlank())
                    && incoming.imageUrl != null) {
                imageUrl = incoming.imageUrl;
            }
            if ((listPageUrl == null || listPageUrl.isBlank())
                    && incoming.listPageUrl != null) {
                listPageUrl = incoming.listPageUrl;
            }
        }
    }

    private boolean isSupportedBenefitText(String text) {
        if (text == null || text.length() < 4 || text.length() > 600) {
            return false;
        }

        String normalized = normalizeText(text);
        if (!normalized.contains("할인")) {
            return false;
        }

        if (normalized.contains("할인 제외")
                || normalized.contains("할인서비스 제외")
                || normalized.contains("제외대상")
                || normalized.contains("연회비")
                || normalized.contains("연체")) {
            return false;
        }

        return DISCOUNT_RATE_PATTERN.matcher(normalized).find()
                || FIXED_DISCOUNT_PATTERN.matcher(normalized).find();
    }

    private BigDecimal extractDiscountRate(String text) {
        Matcher matcher = DISCOUNT_RATE_PATTERN.matcher(text);
        Set<BigDecimal> rates = new LinkedHashSet<>();
        while (matcher.find()) {
            BigDecimal value = new BigDecimal(matcher.group(1));
            if (value.compareTo(BigDecimal.ZERO) > 0) {
                rates.add(value.stripTrailingZeros());
            }
        }
        return rates.size() == 1 ? rates.iterator().next() : null;
    }

    private Integer extractFixedDiscountAmount(String text) {
        if (MONEY_RANGE_PATTERN.matcher(normalizeText(text)).find()) {
            return null;
        }

        Matcher matcher = FIXED_DISCOUNT_PATTERN.matcher(text);
        Set<Integer> amounts = new LinkedHashSet<>();
        while (matcher.find()) {
            extractMoneyValues(matcher.group(1)).stream()
                    .findFirst()
                    .ifPresent(amounts::add);
        }
        return amounts.size() == 1 ? amounts.iterator().next() : null;
    }

    private boolean hasUnrepresentableCondition(String text) {
        String normalized = normalizeText(text);
        return MAXIMUM_RATE_PATTERN.matcher(normalized).find()
                || containsAny(
                normalized,
                "특정 가맹점", "일부 가맹점", "제휴처", "제휴 가맹점",
                "KB Pay", "간편결제", "쿠폰", "사이렌오더",
                "주말", "평일", "요일", "시간대", "첫 결제", "최초 결제",
                "일 1회", "횟수 제한",
                "선택형", "선택 서비스", "패키지"
        );
    }

    private Integer extractAnnualFee(String productText) {
        String text = normalizeText(productText);
        if (text.contains("연회비 없음")) {
            return 0;
        }

        int searchFrom = 0;
        List<Integer> candidates = new ArrayList<>();

        while (true) {
            int annualFeeIndex = text.indexOf("연회비", searchFrom);
            if (annualFeeIndex < 0) {
                break;
            }

            String window = text.substring(
                    annualFeeIndex,
                    Math.min(text.length(), annualFeeIndex + 450)
            );
            candidates.addAll(extractMoneyValues(window));
            searchFrom = annualFeeIndex + 3;
        }

        return candidates.stream()
                .filter(value -> value >= 0 && value <= 5_000_000)
                .max(Comparator.naturalOrder())
                .orElse(0);
    }

    private Integer extractMinimumSpending(String text) {
        String normalized = normalizeText(text);
        if (hasExplicitNoMinimumSpending(normalized)) {
            return null;
        }

        List<String> keywords = List.of(
                "실적 조건", "실적조건",
                "전월 이용실적", "전월이용실적",
                "전월 결제납부실적", "전월결제납부실적",
                "전월 결제회수실적", "전월결제회수실적",
                "전월실적"
        );

        // 전용 상세혜택의 문장 또는 표에 나타나는 첫 실적 구간이
        // 최저 구간이다. 통합한도 표가 뒤에 반복되더라도 앞선 혜택별
        // 실적조건을 우선한다.
        return findFirstMoneyAfterEarliestKeyword(
                normalized,
                keywords,
                260,
                value -> value >= 100_000 && value <= 10_000_000
        );
    }

    private Integer resolveMonthlyLimit(
            String mainText,
            BenefitDetailSection detail,
            Integer minimumSpending,
            Integer fixedAmount
    ) {
        String detailText = detail == null ? "" : detail.text;
        String normalized = normalizeText(mainText + " " + detailText);
        if (hasExplicitNoMonthlyLimit(normalized)) {
            return null;
        }

        List<Integer> candidates = collectMoneyNearKeywords(
                normalized,
                List.of(
                        "최대 할인액", "최대 할인금액",
                        "월 할인한도", "월할인한도",
                        "월간 통합할인한도", "월간통합할인한도",
                        "전월실적별 할인한도", "전월실적별 통합할인한도",
                        "월 할인 제공 이용금액", "월 최대"
                ),
                320,
                value -> value > 0
                        && (minimumSpending == null || value < minimumSpending)
        );

        // 정액 할인이 월 N회로 제한되면 추천 계산기가 사용할 수 있는
        // 월 최대액으로 변환한다.
        if (fixedAmount != null) {
            Matcher frequencyMatcher = MONTHLY_FREQUENCY_PATTERN.matcher(normalized);
            if (frequencyMatcher.find()) {
                int count = parseNumber(frequencyMatcher.group(1));
                candidates.add(fixedAmount * count);
            }
        }

        // 서비스요약 표의 행에 "월 할인한도" 단어가 반복되지 않는
        // 레거시 페이지는 주요혜택과 가장 잘 맞는 행에서 첫 구간 값을 읽는다.
        if (detail != null) {
            String matchingRow = findBestMatchingTableRow(mainText, detail.tableRows);
            if (matchingRow != null) {
                extractMoneyValues(matchingRow).stream()
                        .filter(value -> value > 0)
                        .filter(value -> minimumSpending == null
                                || value < minimumSpending)
                        .forEach(candidates::add);
            }
        }

        return candidates.stream().min(Integer::compareTo).orElse(null);
    }

    private String findBestMatchingTableRow(
            String mainText,
            List<String> tableRows
    ) {
        Set<String> tokens = extractBenefitMatchTokens(mainText);
        String best = null;
        int bestScore = 0;

        for (String row : tableRows) {
            int score = 0;
            for (String token : tokens) {
                if (row.contains(token)) {
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                best = row;
            }
        }
        return bestScore > 0 ? best : null;
    }

    private Integer findFirstMoneyAfterEarliestKeyword(
            String text,
            List<String> keywords,
            int windowLength,
            java.util.function.Predicate<Integer> filter
    ) {
        int searchFrom = 0;
        while (searchFrom < text.length()) {
            int nearestIndex = -1;
            String nearestKeyword = null;

            for (String keyword : keywords) {
                int index = text.indexOf(keyword, searchFrom);
                if (index >= 0 && (nearestIndex < 0 || index < nearestIndex)) {
                    nearestIndex = index;
                    nearestKeyword = keyword;
                }
            }

            if (nearestIndex < 0 || nearestKeyword == null) {
                return null;
            }

            int valueStart = nearestIndex + nearestKeyword.length();
            String window = text.substring(
                    valueStart,
                    Math.min(text.length(), valueStart + windowLength)
            );
            Optional<Integer> first = extractMoneyValues(window).stream()
                    .filter(filter)
                    .findFirst();
            if (first.isPresent()) {
                return first.get();
            }
            searchFrom = valueStart;
        }
        return null;
    }

    private List<Integer> collectMoneyNearKeywords(
            String text,
            List<String> keywords,
            int windowLength,
            java.util.function.Predicate<Integer> filter
    ) {
        List<Integer> candidates = new ArrayList<>();

        for (String keyword : keywords) {
            int searchFrom = 0;
            while (true) {
                int index = text.indexOf(keyword, searchFrom);
                if (index < 0) {
                    break;
                }

                int valueStart = index + keyword.length();
                String window = text.substring(
                        valueStart,
                        Math.min(text.length(), valueStart + windowLength)
                );
                extractMoneyValues(window).stream()
                        .filter(filter)
                        .forEach(candidates::add);
                searchFrom = valueStart;
            }
        }
        return candidates;
    }

    private boolean hasExplicitNoMonthlyLimit(String text) {
        String normalized = normalizeText(text);
        return containsAny(
                normalized,
                "할인한도 없음", "할인 한도 없음", "월 한도 없음"
        );
    }

    private boolean hasExplicitNoMinimumSpending(String text) {
        String normalized = normalizeText(text);
        return containsAny(
                normalized,
                "전월 이용실적 조건 및 할인한도 없음",
                "전월실적 조건 없음", "전월 실적 조건 없음",
                "전월실적 없음", "전월 실적 없음"
        );
    }

    private List<Integer> extractMoneyValues(String text) {
        List<Integer> values = new ArrayList<>();
        List<int[]> occupiedRanges = new ArrayList<>();

        Matcher complexMatcher = COMPLEX_MONEY_PATTERN.matcher(text);
        while (complexMatcher.find()) {
            int man = parseNumber(complexMatcher.group(1));
            int cheon = complexMatcher.group(2) == null
                    ? 0
                    : parseNumber(complexMatcher.group(2));
            values.add(man * 10_000 + cheon * 1_000);
            occupiedRanges.add(new int[]{complexMatcher.start(), complexMatcher.end()});
        }

        Matcher thousandHundredMatcher = THOUSAND_HUNDRED_MONEY_PATTERN.matcher(text);
        while (thousandHundredMatcher.find()) {
            int thousand = parseNumber(thousandHundredMatcher.group(1));
            int hundred = parseNumber(thousandHundredMatcher.group(2));
            values.add(thousand * 1_000 + hundred * 100);
            occupiedRanges.add(new int[]{
                    thousandHundredMatcher.start(),
                    thousandHundredMatcher.end()
            });
        }

        Matcher simpleMatcher = SIMPLE_MONEY_PATTERN.matcher(text);
        while (simpleMatcher.find()) {
            if (overlaps(simpleMatcher.start(), simpleMatcher.end(), occupiedRanges)) {
                continue;
            }
            int number = parseNumber(simpleMatcher.group(1));
            String unit = simpleMatcher.group(2);
            if ("만원".equals(unit)) {
                number *= 10_000;
            } else if ("천원".equals(unit)) {
                number *= 1_000;
            }
            values.add(number);
        }

        return values;
    }

    private boolean overlaps(int start, int end, List<int[]> ranges) {
        for (int[] range : ranges) {
            if (start < range[1] && end > range[0]) {
                return true;
            }
        }
        return false;
    }

    private int parseNumber(String value) {
        return Integer.parseInt(value.replace(",", ""));
    }

    private List<String> resolveCategoryNames(String text) {
        String normalized = normalizeText(text);
        Set<String> categories = new LinkedHashSet<>();
        boolean educationSpecific = containsAny(
                normalized,
                "키즈카페", "문화센터", "학습지"
        );

        if (containsAny(normalized, "동물병원")) {
            categories.add("반려동물");
        } else if (containsAny(normalized, "병원", "약국", "의료", "치과", "건강")) {
            categories.add("병원");
        }
        if (containsAny(normalized, "반려동물", "펫")) {
            categories.add("반려동물");
        }
        if (educationSpecific
                || containsAny(normalized, "학원", "교육", "서점", "독서실")) {
            categories.add("교육");
        }
        if (containsAny(normalized, "해외", "항공", "숙박", "여행", "면세점")) {
            categories.add("여행");
        }
        if (containsAny(normalized,
                "주유", "자동차", "정비", "충전소", "전기차", "수소차",
                "주차", "세차", "EV")) {
            categories.add("자동차");
        }
        if (containsAny(normalized, "대중교통", "버스", "지하철", "택시", "교통")) {
            categories.add("교통");
        }
        if (containsAny(normalized, "미용", "뷰티", "올리브영")) {
            categories.add("뷰티/미용");
        }
        if (containsAny(normalized, "온라인", "쇼핑몰", "쿠팡", "마켓", "백화점", "마트")) {
            categories.add("온라인쇼핑");
        }
        if (!educationSpecific
                && containsAny(normalized, "커피", "카페", "스타벅스")) {
            categories.add("카페");
        }
        if (containsAny(normalized, "음식", "외식", "배달", "패스트푸드", "레스토랑")) {
            categories.add("식비");
        }
        if (containsAny(normalized,
                "아파트관리비", "관리비", "이동통신", "통신", "휴대폰", "모바일")) {
            categories.add("주거/통신");
        }
        if (containsAny(normalized, "KB Pay", "간편결제", "금융")) {
            categories.add("금융");
        }
        if (!educationSpecific && containsAny(normalized,
                "편의점", "구독", "OTT", "영화", "문화",
                "PC방", "레저", "스포츠", "생활요금")) {
            categories.add("생활");
        }

        // 전가맹점 할인은 현재 단일 소비 카테고리 구조로 정확히 표현할 수 없다.
        return new ArrayList<>(categories);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private List<CrawledCardBenefitDTO> deduplicateBenefits(
            List<CrawledCardBenefitDTO> benefits
    ) {
        Map<String, CrawledCardBenefitDTO> unique = new LinkedHashMap<>();

        for (CrawledCardBenefitDTO benefit : benefits) {
            if (benefit == null || benefit.getCategoryName() == null) {
                continue;
            }
            if (benefit.getBenefitRate() == null
                    && benefit.getBenefitAmount() == null) {
                continue;
            }

            // 현재 추천 모델은 가맹점 조건을 저장하지 않으므로 카드 한 장에서
            // 같은 카테고리 혜택을 여러 개 유지하면 동일 거래가 중복 계산된다.
            unique.merge(
                    benefit.getCategoryName(),
                    benefit,
                    this::chooseMoreConservativeBenefit
            );
        }

        return new ArrayList<>(unique.values());
    }

    private CrawledCardBenefitDTO chooseMoreConservativeBenefit(
            CrawledCardBenefitDTO left,
            CrawledCardBenefitDTO right
    ) {
        CrawledCardBenefitDTO selected;

        long leftLimit = left.getMonthlyLimit() == null
                ? Long.MAX_VALUE
                : left.getMonthlyLimit();
        long rightLimit = right.getMonthlyLimit() == null
                ? Long.MAX_VALUE
                : right.getMonthlyLimit();

        if (leftLimit != rightLimit) {
            selected = leftLimit < rightLimit ? left : right;
        } else if (left.getBenefitRate() != null
                && right.getBenefitRate() != null) {
            selected = left.getBenefitRate().compareTo(right.getBenefitRate()) <= 0
                    ? left
                    : right;
        } else if (left.getBenefitAmount() != null
                && right.getBenefitAmount() != null) {
            selected = left.getBenefitAmount() <= right.getBenefitAmount()
                    ? left
                    : right;
        } else {
            selected = left;
        }

        Integer stricterMinimum = maxNullable(
                left.getMinimumSpendingAmount(),
                right.getMinimumSpendingAmount()
        );
        selected.setMinimumSpendingAmount(stricterMinimum);
        return selected;
    }

    private Integer maxNullable(Integer left, Integer right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return Math.max(left, right);
    }

    private Element findProductContainer(Document document, String cardName) {
        Element heading = document.select("h1").stream()
                .filter(element -> normalizeText(element.text()).contains(cardName)
                        || cardName.contains(normalizeText(element.text())))
                .findFirst()
                .orElse(document.selectFirst("h1"));

        if (heading == null) {
            return document.body();
        }

        Element current = heading;
        Element best = heading.parent() == null ? heading : heading.parent();

        while (current.parent() != null) {
            current = current.parent();
            String text = normalizeText(current.text());
            if (text.contains("연회비") && text.contains("상세혜택")) {
                best = current;
                if (text.length() >= 500
                        && countOccurrences(text, "상세혜택") >= 2) {
                    return current;
                }
            }
            if (current == document.body()) {
                break;
            }
        }

        return best;
    }

    private int countOccurrences(String text, String token) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    private String resolveDetailCardName(Element container, String fallback) {
        for (Element heading : container.select("h1")) {
            String text = normalizeText(heading.text());
            if (isValidCardName(text)) {
                return text;
            }
        }
        return fallback;
    }

    private String resolveProductDescription(String summary, Element container) {
        String normalizedSummary = normalizeText(summary);
        if (!normalizedSummary.isBlank()) {
            return truncate(normalizedSummary, MAX_DESCRIPTION_LENGTH);
        }

        Element heading = container.selectFirst("h1");
        if (heading != null) {
            Element next = heading.nextElementSibling();
            if (next != null) {
                return truncate(normalizeText(next.text()), MAX_DESCRIPTION_LENGTH);
            }
        }
        return null;
    }

    private String resolveImageUrl(Element imageElement) {
        String imageUrl = imageElement.absUrl("src");
        if (imageUrl.isBlank()) {
            imageUrl = imageElement.absUrl("data-src");
        }
        if (imageUrl.isBlank()) {
            imageUrl = imageElement.attr("src");
        }
        if (imageUrl.isBlank()) {
            imageUrl = imageElement.attr("data-src");
        }

        if (imageUrl.isBlank()) {
            return "";
        }
        if (imageUrl.startsWith("//")) {
            return "https:" + imageUrl;
        }
        if (imageUrl.startsWith("/")) {
            return KB_CARD_BASE_URL + imageUrl;
        }
        return imageUrl;
    }

    private boolean isLikelyCardImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return false;
        }

        String lower = imageUrl.toLowerCase();
        if (lower.contains("logo-")
                || lower.contains("heritage")
                || lower.contains("/logo")) {
            return false;
        }

        return lower.contains("crd")
                || lower.contains("card")
                || PRODUCT_CODE_PATTERN.matcher(lower).find();
    }

    private boolean isValidCardName(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String value = normalizeText(text);
        return value.length() > 1
                && !value.matches(".*\\d+위.*")
                && !value.contains("발급정지")
                && !value.contains("카드이미지")
                && !value.equals("이미지")
                && !value.contains("연회비")
                && !value.contains("자세히 보기");
    }

    private String cleanCardNameText(String text) {
        return normalizeText(text)
                .replaceAll("\\[.*?]", "")
                .replaceAll("(?:신용|체크)발급\\s*\\d+위", "")
                .trim();
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        String normalized = normalizeText(text);
        return normalized.length() <= maxLength
                ? normalized
                : normalized.substring(0, maxLength);
    }

    private String downloadAndSaveImage(String imageUrl) {
        try {
            File directory = new File(getSaveDirectory());
            if (!directory.exists() && !directory.mkdirs()) {
                log.warn("[카탈로그 매니저] 카드 이미지 폴더 생성 실패: {}",
                        directory.getAbsolutePath());
                return null;
            }

            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf('?'));
            }
            if (!fileName.toLowerCase().endsWith(".png")
                    && !fileName.toLowerCase().endsWith(".jpg")
                    && !fileName.toLowerCase().endsWith(".jpeg")) {
                fileName = fileName + ".png";
            }

            File targetFile = new File(directory, fileName);
            if (targetFile.exists() && targetFile.length() > 2000) {
                return fileName;
            }

            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", DESKTOP_USER_AGENT);
            connection.setRequestProperty("Referer", KB_CARD_BASE_URL + "/");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    Files.copy(
                            inputStream,
                            targetFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );

                    if (targetFile.length() < 2000) {
                        Files.deleteIfExists(targetFile.toPath());
                        return null;
                    }
                    return fileName;
                }
            }
        } catch (Exception e) {
            log.debug("[카탈로그 매니저] 카드 이미지 저장 실패: url={}, reason={}",
                    imageUrl, e.getMessage());
        }
        return null;
    }

    private void logCollectedCatalog(List<CrawledCardProductDTO> products) {
        int benefitCount = products.stream()
                .map(CrawledCardProductDTO::getBenefits)
                .mapToInt(List::size)
                .sum();

        log.info("==================================================");
        log.info("[카탈로그 매니저] KB카드 공식 목록 크롤링 완료: 카드 {}건, 혜택 {}건",
                products.size(), benefitCount);
        for (Map.Entry<String, String> entry
                : catalogRepository.getAllCatalog().entrySet()) {
            log.info("   카드명: [{}] ==> 파일명: [{}]",
                    entry.getKey(), entry.getValue());
        }
        log.info("==================================================");
    }

    private void loadFallbackSeed() {
        catalogRepository.putCardInfo(
                "KB Pay 노리2 체크카드",
                "09297_img.png"
        );
        catalogRepository.putCardInfo(
                "KB국민 톡톡MyPoint 카드",
                "09129_img.png"
        );
        log.info("[카탈로그 매니저] 기본 메모리 카드 매핑 적용. DB 데이터는 유지합니다.");
    }
}
