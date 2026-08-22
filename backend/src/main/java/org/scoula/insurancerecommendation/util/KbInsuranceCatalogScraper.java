package org.scoula.insurancerecommendation.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.scoula.common.util.UploadPathName;
import org.scoula.insurancerecommendation.dto.CrawledInsuranceCoverageDTO;
import org.scoula.insurancerecommendation.dto.CrawledInsuranceProductDTO;
import org.scoula.insurancerecommendation.mapper.InsuranceRecommendationMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 서버 시작 시 KB손해보험의 전체 상품 메뉴를 읽고, 대표 이미지를 실제로
 * 내려받은 상품만으로 보험 카탈로그를 다시 만든다.
 *
 * <p>모든 네트워크 수집과 이미지 보유 상품 검증을 마친 뒤에만 기존 보험
 * 테이블을 TRUNCATE한다. 이미지가 없는 상품은 상품/보장/추천 매핑 어디에도
 * 저장하지 않는다.</p>
 */
@Component
public class KbInsuranceCatalogScraper implements InitializingBean {

    private static final Logger log =
            LogManager.getLogger(KbInsuranceCatalogScraper.class);

    private static final String BASE_URL = "https://www.kbinsure.co.kr/";
    private static final String CATALOG_SEED_URL =
            BASE_URL + "CG302130001.ec";
    private static final String FLOOD_EARTHQUAKE_PRODUCT_CODE =
            "CG305060001";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                    + "AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/124.0 Safari/537.36";

    private static final int REQUEST_TIMEOUT_MILLIS = 10_000;
    private static final int DETAIL_WORKER_COUNT = 5;
    private static final int MINIMUM_PRODUCT_COUNT = 35;
    /* 이미지 서버 장애로 일부 상품만 남은 상태에서 DB를 비우지 않기 위한 하한선. */
    private static final int MINIMUM_IMAGE_PRODUCT_COUNT = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 350;
    private static final int MAX_COVERAGE_DESCRIPTION_LENGTH = 500;
    private static final int MAX_COVERAGE_COUNT = 3;

    private static final Pattern CONTENT_LINK_PATTERN = Pattern.compile(
            "content\\('([^']+)'\\)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CSS_URL_PATTERN = Pattern.compile(
            "url\\(\\s*(['\"]?)([^'\"\\)]+)\\1\\s*\\)",
            Pattern.CASE_INSENSITIVE
    );

    /* 여러 상품이 함께 사용하는 공용 CSS를 상품마다 다시 요청하지 않는다. */
    private static final Map<String, String> STYLESHEET_CACHE =
            new ConcurrentHashMap<>();

    private static final Map<String, String> BIZ_SOURCE_CODES = Map.of(
            "goEtprPrptTotins", "BIZ_ENTERPRISE",
            "goCargoIns", "BIZ_CARGO",
            "goPlIns", "BIZ_PL"
    );

    private static final Map<String, String> BIZ_APPLICATION_URLS = Map.of(
            "goEtprPrptTotins", "https://www.kbib2b.com/b2b/BG105010001.ec",
            "goCargoIns", "https://www.kbib2b.com/b2b/BG101010001.ec",
            "goPlIns", "https://www.kbib2b.com/b2b/BG103010001.ec"
    );

    private static final List<InsuranceMatchRule> MATCH_RULES = List.of(
            new InsuranceMatchRule(
                    "CG302290001", "병원", "건강·실비",
                    "최근 병원 이용 내역을 바탕으로 건강 보장 상품을 추천합니다.", 1
            ),
            new InsuranceMatchRule(
                    "CG302090001", "병원", "건강·실비",
                    "최근 병원 이용 내역을 바탕으로 실손의료비 보장 상품을 추천합니다.", 2
            ),
            new InsuranceMatchRule(
                    "CG302250001", "병원", "건강·실비",
                    "최근 병원 이용 내역을 바탕으로 간편가입 실손 상품을 추천합니다.", 3
            ),
            new InsuranceMatchRule(
                    "CG308080001", "여행", "여행자",
                    "최근 여행 관련 소비가 있어 해외여행보험을 추천합니다.", 1
            ),
            new InsuranceMatchRule(
                    "CG308040001:STUDY", "여행", "여행자",
                    "최근 여행 관련 소비가 있어 유학·연수 장기체류 보험을 함께 추천합니다.", 2
            ),
            new InsuranceMatchRule(
                    "CG308040001:BUSINESS", "여행", "여행자",
                    "최근 여행 관련 소비가 있어 출장·주재원 보험을 함께 추천합니다.", 3
            ),
            new InsuranceMatchRule(
                    "CG301010012", "자동차", "운전자",
                    "최근 자동차 관련 소비가 있어 자동차보험을 추천합니다.", 1
            ),
            new InsuranceMatchRule(
                    "CG301040001", "자동차", "운전자",
                    "최근 자동차 관련 소비가 있어 운전자 상해보험을 추천합니다.", 2
            ),
            new InsuranceMatchRule(
                    "CG302130001", "치과", "치아",
                    "최근 치과 이용 내역이 있어 치아보험을 추천합니다.", 1
            ),
            new InsuranceMatchRule(
                    "CG313010001", "반려동물", "펫",
                    "최근 반려동물 관련 소비가 있어 강아지 펫보험을 추천합니다.", 1
            ),
            new InsuranceMatchRule(
                    "CG313020001", "반려동물", "펫",
                    "최근 반려동물 관련 소비가 있어 고양이 펫보험을 추천합니다.", 2
            )
    );

    private final InsuranceRecommendationMapper insuranceRecommendationMapper;
    private final DataSource dataSource;

    public KbInsuranceCatalogScraper(
            InsuranceRecommendationMapper insuranceRecommendationMapper,
            DataSource dataSource
    ) {
        this.insuranceRecommendationMapper = insuranceRecommendationMapper;
        this.dataSource = dataSource;
    }
    // 보험 크롤링 활성화
    @Override
    public void afterPropertiesSet() {
        synchronizeInsuranceCatalog();
    }

    public void synchronizeInsuranceCatalog() {
        log.info("==================================================");
        log.info("[보험 카탈로그] 대표 이미지 보유 상품 자동 동기화 시작");
        log.info("==================================================");

        try {
            List<CrawledInsuranceProductDTO> products = crawlAllProducts();

            if (!isSafeCatalog(products)) {
                log.error(
                        "[보험 카탈로그] 수집 결과 검증 실패로 기존 DB를 유지합니다. productCount={}",
                        products.size()
                );
                return;
            }

            replaceCatalog(products);
            logCatalogSummary(products);
        } catch (Exception e) {
            log.error(
                    "[보험 카탈로그] 동기화 실패로 기존 DB를 유지합니다: {}",
                    e.getMessage(),
                    e
            );
        }
    }

    /* 전체 메뉴를 탐색하되 대표 이미지 저장에 성공한 상품만 반환한다. */
    List<CrawledInsuranceProductDTO> crawlAllProducts() throws Exception {
        Document seedDocument = getDocument(CATALOG_SEED_URL);
        List<MenuProduct> menuProducts = parseProductMenu(seedDocument);

        if (menuProducts.size() < MINIMUM_PRODUCT_COUNT) {
            throw new IllegalStateException(
                    "보험 상품 메뉴가 충분히 수집되지 않았습니다: " + menuProducts.size()
            );
        }

        ExecutorService executor = Executors.newFixedThreadPool(DETAIL_WORKER_COUNT);
        try {
            List<Callable<CrawledInsuranceProductDTO>> tasks = menuProducts.stream()
                    .map(menuProduct -> (Callable<CrawledInsuranceProductDTO>)
                            () -> crawlProductDetail(menuProduct))
                    .toList();

            List<Future<CrawledInsuranceProductDTO>> futures =
                    executor.invokeAll(tasks);
            Map<String, CrawledInsuranceProductDTO> productsBySourceCode =
                    new LinkedHashMap<>();

            for (Future<CrawledInsuranceProductDTO> future : futures) {
                CrawledInsuranceProductDTO product = future.get();
                if (product == null) {
                    continue;
                }

                if ("CG308040001".equals(product.getSourceCode())) {
                    for (CrawledInsuranceProductDTO split : splitLongStayProduct(product)) {
                        productsBySourceCode.put(split.getSourceCode(), split);
                    }
                } else {
                    productsBySourceCode.put(product.getSourceCode(), product);
                }
            }

            return new ArrayList<>(productsBySourceCode.values());
        } finally {
            executor.shutdownNow();
        }
    }

    private List<MenuProduct> parseProductMenu(Document document) {
        Element menu = document.selectFirst("#lnb nav ul.depth1");
        if (menu == null) {
            throw new IllegalStateException("KB손해보험 상품 메뉴를 찾지 못했습니다.");
        }

        List<MenuProduct> result = new ArrayList<>();
        Set<String> seenSourceCodes = new LinkedHashSet<>();

        for (Element group : menu.children()) {
            Element groupLink = firstDirectChild(group, "a");
            Element leafList = firstDirectChild(group, "ul");
            if (groupLink == null || leafList == null) {
                continue;
            }

            String sourceCategory = normalizeText(groupLink.text());
            for (Element leaf : leafList.children()) {
                Element link = firstDirectChild(leaf, "a");
                if (link == null) {
                    continue;
                }

                String insuranceName = normalizeText(link.text());
                if (!isInsuranceProductName(insuranceName)) {
                    continue;
                }

                String href = link.attr("href");
                String sourceCode = resolveSourceCode(href, insuranceName);
                if (sourceCode == null || !seenSourceCodes.add(sourceCode)) {
                    continue;
                }

                String applicationUrl = resolveApplicationUrl(href);
                result.add(new MenuProduct(
                        sourceCode,
                        insuranceName,
                        sourceCategory,
                        resolveProjectCategory(insuranceName, sourceCategory),
                        applicationUrl
                ));
            }
        }

        return result;
    }

    private CrawledInsuranceProductDTO crawlProductDetail(MenuProduct menuProduct) {
        String description = createFallbackDescription(menuProduct);
        String imageFileName = null;
        List<CrawledInsuranceCoverageDTO> coverages = new ArrayList<>();

        /*
         * 치아보험(CG302130001)은 전체 상품 메뉴를 읽는 시작 페이지와 URL이 같지만,
         * 같은 문서 안에 실제 상품 설명·보장·대표 이미지가 함께 존재한다.
         * 시작 페이지도 다른 상품과 동일하게 상세 수집해야 한다.
         */
        try {
            Document detail = getDocument(menuProduct.applicationUrl());
            description = resolveDescription(detail, menuProduct);
            imageFileName = downloadProductImage(detail, menuProduct.sourceCode());
            coverages = parseCoverages(detail, menuProduct);
        } catch (Exception e) {
            log.warn(
                    "[보험 카탈로그] 상세 수집 실패: code={}, name={}, reason={}",
                    menuProduct.sourceCode(),
                    menuProduct.insuranceName(),
                    e.getMessage()
            );
        }

        if (imageFileName == null || imageFileName.isBlank()) {
            log.info(
                    "[보험 카탈로그] 대표 이미지가 없어 상품을 제외합니다: code={}, name={}",
                    menuProduct.sourceCode(),
                    menuProduct.insuranceName()
            );
            return null;
        }

        if (coverages.isEmpty()) {
            coverages.add(createFallbackCoverage(menuProduct.insuranceName()));
        }

        CrawledInsuranceProductDTO product = new CrawledInsuranceProductDTO();
        product.setSourceCode(menuProduct.sourceCode());
        product.setInsuranceName(menuProduct.insuranceName());
        product.setInsuranceCategory(menuProduct.projectCategory());
        product.setInsuranceDescription(description);
        product.setMonthlyPremium(null);
        product.setInsuranceImage(imageFileName);
        product.setApplicationUrl(menuProduct.applicationUrl());
        product.setCoverages(coverages);
        return product;
    }

    private List<CrawledInsuranceProductDTO> splitLongStayProduct(
            CrawledInsuranceProductDTO original
    ) {
        CrawledInsuranceProductDTO study = copyProduct(
                original,
                "CG308040001:STUDY",
                "해외장기체류(유학연수생)보험",
                "해외 유학이나 연수 중 발생할 수 있는 상해와 질병 등의 위험을 안내하는 장기체류 보험입니다.",
                "유학·연수 중 주요 보장"
        );
        CrawledInsuranceProductDTO business = copyProduct(
                original,
                "CG308040001:BUSINESS",
                "해외장기체류(출장주재원)보험",
                "해외 출장이나 주재 중 발생할 수 있는 상해와 질병 등의 위험을 안내하는 장기체류 보험입니다.",
                "출장·주재 중 주요 보장"
        );
        return List.of(study, business);
    }

    private CrawledInsuranceProductDTO copyProduct(
            CrawledInsuranceProductDTO original,
            String sourceCode,
            String insuranceName,
            String description,
            String coverageName
    ) {
        CrawledInsuranceCoverageDTO coverage = new CrawledInsuranceCoverageDTO();
        coverage.setCoverageName(coverageName);
        coverage.setCoverageAmount(null);
        coverage.setCoverageDescription(
                "해외 장기체류 중 발생할 수 있는 주요 위험을 상품 안내와 약관에 따라 보장합니다."
        );
        coverage.setCoverageLimit(null);

        CrawledInsuranceProductDTO product = new CrawledInsuranceProductDTO();
        product.setSourceCode(sourceCode);
        product.setInsuranceName(insuranceName);
        product.setInsuranceCategory("여행자");
        product.setInsuranceDescription(description);
        product.setMonthlyPremium(null);
        product.setInsuranceImage(original.getInsuranceImage());
        product.setApplicationUrl(original.getApplicationUrl());
        product.setCoverages(List.of(coverage));
        return product;
    }

    private String resolveDescription(Document document, MenuProduct product) {
        String[] selectors = {
                ".prdt_inform_visual .smallText",
                ".prdt_inform_visual .bigText",
                ".product_visual .txt",
                ".visual_txt",
                "meta[name=description]"
        };

        for (String selector : selectors) {
            Element element = document.selectFirst(selector);
            if (element == null) {
                continue;
            }

            String text = element.tagName().equals("meta")
                    ? element.attr("content")
                    : element.text();
            text = cleanProductText(text, product.insuranceName());
            if (text.length() >= 12) {
                return truncate(text, MAX_DESCRIPTION_LENGTH);
            }
        }

        return createFallbackDescription(product);
    }

    private List<CrawledInsuranceCoverageDTO> parseCoverages(
            Document document,
            MenuProduct product
    ) {
        Elements headings = document.select(
                "h4.tit_num, h3.tit_num, .new_prdt_inform h4, "
                        + ".prdt_inform_cont h4, .product_info h3"
        );
        Map<String, CrawledInsuranceCoverageDTO> unique = new LinkedHashMap<>();

        for (Element heading : headings) {
            String coverageName = cleanCoverageHeading(heading.text());
            if (!isUsefulCoverageHeading(coverageName)) {
                continue;
            }

            String description = findCoverageDescription(heading);
            if (description.isBlank()) {
                description = coverageName
                        + "에 관한 상세 보장 조건은 상품 안내 페이지와 약관에서 확인할 수 있습니다.";
            }

            CrawledInsuranceCoverageDTO coverage = new CrawledInsuranceCoverageDTO();
            coverage.setCoverageName(truncate(coverageName, 200));
            coverage.setCoverageAmount(null);
            coverage.setCoverageDescription(
                    truncate(description, MAX_COVERAGE_DESCRIPTION_LENGTH)
            );
            coverage.setCoverageLimit(null);
            unique.putIfAbsent(coverageName, coverage);

            if (unique.size() >= MAX_COVERAGE_COUNT) {
                break;
            }
        }

        if (unique.isEmpty()) {
            unique.put(
                    product.insuranceName(),
                    createFallbackCoverage(product.insuranceName())
            );
        }
        return new ArrayList<>(unique.values());
    }

    private String findCoverageDescription(Element heading) {
        Element container = heading.parent();
        if (container == null) {
            return "";
        }

        Element candidate = container.selectFirst(
                "ul.insPrdt_bul li, ul li.fs18, p:not(.tit), .txt"
        );
        if (candidate == null) {
            return "";
        }
        return cleanProductText(candidate.text(), "");
    }

    private CrawledInsuranceCoverageDTO createFallbackCoverage(String insuranceName) {
        CrawledInsuranceCoverageDTO coverage = new CrawledInsuranceCoverageDTO();
        coverage.setCoverageName("주요 보장 안내");
        coverage.setCoverageAmount(null);
        coverage.setCoverageDescription(
                insuranceName
                        + "의 구체적인 보장 항목과 가입 조건은 공식 상품 안내 페이지에서 확인할 수 있습니다."
        );
        coverage.setCoverageLimit(null);
        return coverage;
    }

    private String downloadProductImage(Document document, String sourceCode) {
        /*
         * 풍수해·지진재해보험 페이지는 대표 집 그림을 CSS background로
         * 제공하고, 가입문의 전화기 아이콘을 일반 img 태그로 먼저 노출한다.
         * 공통 img 우선 탐색을 적용하면 전화기 아이콘이 대표 이미지로
         * 저장되므로 이 상품만 대표 visual의 CSS 배경을 먼저 확인한다.
         */
        if (FLOOD_EARTHQUAKE_PRODUCT_CODE.equals(sourceCode)) {
            String backgroundImageUrl = resolveBackgroundImageUrl(document);
            if (isAllowedInsuranceImage(backgroundImageUrl)) {
                String saved = downloadAndSaveImage(
                        backgroundImageUrl,
                        sourceCode
                );
                if (saved != null) {
                    return saved;
                }
            }

            /* 대표 배경 수집에 실패해도 전화기/본문 아이콘으로 대체하지 않는다. */
            return null;
        }

        Elements images = document.select(
                ".prdt_inform_visual img, .product_visual img, "
                        + "img[src*=/images/ins_prdt/]"
        );

        for (Element image : images) {
            String imageUrl = image.absUrl("src");
            if (imageUrl.isBlank()) {
                imageUrl = resolveAbsoluteUrl(image.attr("src"));
            }
            if (!isAllowedInsuranceImage(imageUrl)) {
                continue;
            }

            String saved = downloadAndSaveImage(imageUrl, sourceCode);
            if (saved != null) {
                return saved;
            }
        }

        /*
         * KB자동차보험처럼 대표 이미지를 img가 아닌 CSS background로
         * 제공하는 상품을 위해 인라인 스타일과 외부 스타일시트를 추가 확인한다.
         */
        String backgroundImageUrl = resolveBackgroundImageUrl(document);
        if (isAllowedInsuranceImage(backgroundImageUrl)) {
            return downloadAndSaveImage(backgroundImageUrl, sourceCode);
        }
        return null;
    }

    private String resolveBackgroundImageUrl(Document document) {
        Elements visualElements = document.select(
                "#contents [class*=visual], #contents [class*=Visual], "
                        + "#contents [class*=banner], #contents [class*=Banner], "
                        + "#contents [class*=hero], #contents [class*=Hero], "
                        + "#contents [style*=background], "
                        + ".prdt_inform_visual, .product_visual, "
                        + "[class*=visual], [class*=Visual]"
        );

        /* style="background-image:url(...)" 형태를 먼저 확인한다. */
        for (Element element : visualElements) {
            String inlineStyle = element.attr("style");
            if (!inlineStyle.toLowerCase(Locale.ROOT).contains("background")) {
                continue;
            }

            String rawImageUrl = extractCssImageUrl(inlineStyle);
            String imageUrl = resolveUrl(document.location(), rawImageUrl);
            if (isAllowedInsuranceImage(imageUrl)) {
                return imageUrl;
            }
        }

        /* 외부 CSS에서 현재 상세 페이지의 visual 클래스/ID 규칙을 찾는다. */
        Set<String> visualSelectors = new LinkedHashSet<>();
        for (Element element : visualElements) {
            for (String className : element.classNames()) {
                visualSelectors.add("." + className);
            }
            if (!element.id().isBlank()) {
                visualSelectors.add("#" + element.id());
            }
        }

        for (Element stylesheet : document.select("link[rel=stylesheet][href]")) {
            String stylesheetUrl = stylesheet.absUrl("href");
            if (stylesheetUrl.isBlank()) {
                stylesheetUrl = resolveUrl(
                        document.location(),
                        stylesheet.attr("href")
                );
            }
            if (!isAllowedStylesheet(stylesheetUrl)) {
                continue;
            }

            String css = getStylesheet(stylesheetUrl);
            if (css.isBlank()) {
                continue;
            }

            for (String selector : visualSelectors) {
                String declaration = findCssDeclaration(css, selector);
                if (declaration.isBlank()
                        || !declaration.toLowerCase(Locale.ROOT)
                        .contains("background")) {
                    continue;
                }

                String rawImageUrl = extractCssImageUrl(declaration);
                String imageUrl = resolveUrl(stylesheetUrl, rawImageUrl);
                if (isAllowedInsuranceImage(imageUrl)) {
                    return imageUrl;
                }
            }
        }
        return "";
    }

    private String getStylesheet(String stylesheetUrl) {
        return STYLESHEET_CACHE.computeIfAbsent(stylesheetUrl, url -> {
            try {
                return Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .referrer(CATALOG_SEED_URL)
                        .timeout(REQUEST_TIMEOUT_MILLIS)
                        .ignoreContentType(true)
                        .execute()
                        .body();
            } catch (Exception e) {
                log.debug(
                        "[보험 카탈로그] CSS 수집 실패: url={}, reason={}",
                        url,
                        e.getMessage()
                );
                return "";
            }
        });
    }

    private String findCssDeclaration(String css, String selector) {
        Pattern rulePattern = Pattern.compile(
                Pattern.quote(selector) + "(?![a-zA-Z0-9_-])[^\\{]*\\{([^}]*)}",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher matcher = rulePattern.matcher(css);
        while (matcher.find()) {
            String declaration = matcher.group(1);
            if (declaration.toLowerCase(Locale.ROOT).contains("background")) {
                return declaration;
            }
        }
        return "";
    }

    private String extractCssImageUrl(String cssDeclaration) {
        if (cssDeclaration == null || cssDeclaration.isBlank()) {
            return "";
        }
        Matcher matcher = CSS_URL_PATTERN.matcher(cssDeclaration);
        return matcher.find() ? matcher.group(2).trim() : "";
    }

    private boolean isAllowedStylesheet(String stylesheetUrl) {
        if (stylesheetUrl == null || stylesheetUrl.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(stylesheetUrl);
            String host = uri.getHost();
            return host != null
                    && (host.equals("www.kbinsure.co.kr")
                    || host.endsWith(".kbinsure.co.kr"));
        } catch (Exception e) {
            return false;
        }
    }

    private String resolveUrl(String baseUrl, String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        try {
            return URI.create(baseUrl).resolve(path.trim()).toString();
        } catch (Exception e) {
            return resolveAbsoluteUrl(path);
        }
    }

    private String downloadAndSaveImage(String imageUrl, String sourceCode) {
        HttpURLConnection connection = null;
        try {
            File directory = new File(UploadPathName.getInsurancePath());
            if (!directory.exists() && !directory.mkdirs()) {
                return null;
            }

            URL url = URI.create(imageUrl).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Referer", CATALOG_SEED_URL);
            connection.setConnectTimeout(REQUEST_TIMEOUT_MILLIS);
            connection.setReadTimeout(REQUEST_TIMEOUT_MILLIS);

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            String contentType = connection.getContentType();
            if (contentType == null
                    || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
                return null;
            }

            String originalName = new File(url.getPath()).getName();
            String extension = resolveImageExtension(originalName, contentType);
            String fileName = sanitizeFileName(sourceCode) + extension;
            File target = new File(directory, fileName);

            /*
             * 과거 실행에서 전화기 아이콘이 같은 상품 코드 파일명으로
             * 저장됐을 수 있으므로 해당 상품은 올바른 배경 이미지로 덮어쓴다.
             */
            if (!FLOOD_EARTHQUAKE_PRODUCT_CODE.equals(sourceCode)
                    && target.isFile()
                    && target.length() > 1_000) {
                return fileName;
            }

            try (InputStream inputStream = connection.getInputStream()) {
                Files.copy(
                        inputStream,
                        target.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            if (target.length() < 1_000) {
                Files.deleteIfExists(target.toPath());
                return null;
            }
            return fileName;
        } catch (Exception e) {
            log.debug(
                    "[보험 카탈로그] 이미지 저장 실패: url={}, reason={}",
                    imageUrl,
                    e.getMessage()
            );
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private boolean isSafeCatalog(List<CrawledInsuranceProductDTO> products) {
        if (products.size() < MINIMUM_IMAGE_PRODUCT_COUNT) {
            log.error(
                    "[보험 카탈로그] 이미지 수집 상품이 안전 기준보다 적습니다: count={}, minimum={}",
                    products.size(),
                    MINIMUM_IMAGE_PRODUCT_COUNT
            );
            return false;
        }

        return products.stream().allMatch(product ->
                product.getInsuranceName() != null
                        && !product.getInsuranceName().isBlank()
                        && product.getInsuranceCategory() != null
                        && product.getInsuranceImage() != null
                        && !product.getInsuranceImage().isBlank()
                        && product.getApplicationUrl() != null
                        && product.getApplicationUrl().startsWith("https://")
        );
    }

    private void replaceCatalog(List<CrawledInsuranceProductDTO> products)
            throws Exception {
        truncateInsuranceTables();

        int insertedProducts = 0;
        int insertedCoverages = 0;
        int insertedMatches = 0;
        Map<String, CrawledInsuranceProductDTO> productsBySourceCode =
                new LinkedHashMap<>();

        for (CrawledInsuranceProductDTO product : products) {
            int rows = insuranceRecommendationMapper
                    .insertCrawledInsuranceProduct(product);
            if (rows != 1 || product.getInsuranceProductId() == null) {
                throw new IllegalStateException(
                        "보험 상품 저장 실패: " + product.getInsuranceName()
                );
            }
            insertedProducts += rows;
            productsBySourceCode.put(product.getSourceCode(), product);

            for (CrawledInsuranceCoverageDTO coverage : product.getCoverages()) {
                coverage.setInsuranceProductId(product.getInsuranceProductId());
                insertedCoverages += insuranceRecommendationMapper
                        .insertCrawledInsuranceCoverage(coverage);
            }
        }

        for (InsuranceMatchRule rule : MATCH_RULES) {
            CrawledInsuranceProductDTO product =
                    productsBySourceCode.get(rule.sourceCode());
            if (product == null) {
                log.info(
                        "[보험 카탈로그] 이미지 미수집 상품의 추천 매핑을 생략합니다: code={}",
                        rule.sourceCode()
                );
                continue;
            }
            if (!rule.projectCategory().equals(product.getInsuranceCategory())) {
                throw new IllegalStateException(
                        "추천 매핑 상품 카테고리 불일치: " + rule.sourceCode()
                                + " (예상=" + rule.projectCategory()
                                + ", 실제=" + product.getInsuranceCategory() + ")"
                );
            }

            int rows = insuranceRecommendationMapper.insertCrawledInsuranceMatch(
                    product.getInsuranceProductId(),
                    rule.spendingCategoryName(),
                    rule.recommendationReason(),
                    rule.priority()
            );
            if (rows != 1) {
                throw new IllegalStateException(
                        "소비 카테고리 매핑 실패: " + rule.spendingCategoryName()
                );
            }
            insertedMatches += rows;
        }

        log.info(
                "[보험 카탈로그] DB 전체 교체 완료: 상품 {}건, 보장 {}건, 추천매핑 {}건",
                insertedProducts,
                insertedCoverages,
                insertedMatches
        );
    }

    private void truncateInsuranceTables() throws Exception {
        try (java.sql.Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            try {
                statement.executeUpdate(
                        "TRUNCATE TABLE kb_insurance_recommendation_tbl"
                );
                statement.executeUpdate(
                        "TRUNCATE TABLE kb_insurance_category_match_tbl"
                );
                statement.executeUpdate(
                        "TRUNCATE TABLE kb_insurance_coverage_tbl"
                );
                statement.executeUpdate(
                        "TRUNCATE TABLE kb_insurance_product_tbl"
                );
            } finally {
                statement.execute("SET FOREIGN_KEY_CHECKS = 1");
            }
        }
    }

    private Document getDocument(String url) throws Exception {
        Connection.Response response = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .referrer(CATALOG_SEED_URL)
                .timeout(REQUEST_TIMEOUT_MILLIS)
                .followRedirects(true)
                .execute();
        return response.parse();
    }

    private Element firstDirectChild(Element parent, String tagName) {
        for (Element child : parent.children()) {
            if (child.tagName().equals(tagName)) {
                return child;
            }
        }
        return null;
    }

    private boolean isInsuranceProductName(String name) {
        return name != null
                && name.contains("보험")
                && !name.contains("특약")
                && !name.contains("보험료");
    }

    private String resolveSourceCode(String href, String insuranceName) {
        Matcher matcher = CONTENT_LINK_PATTERN.matcher(href);
        if (matcher.find()) {
            String path = URLDecoder.decode(
                    matcher.group(1),
                    StandardCharsets.UTF_8
            );
            int queryIndex = path.indexOf('?');
            String fileName = queryIndex >= 0 ? path.substring(0, queryIndex) : path;
            int dotIndex = fileName.indexOf('.');
            return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        }

        for (Map.Entry<String, String> entry : BIZ_SOURCE_CODES.entrySet()) {
            if (href.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "MENU_" + Math.abs(insuranceName.hashCode());
    }

    private String resolveApplicationUrl(String href) {
        Matcher matcher = CONTENT_LINK_PATTERN.matcher(href);
        if (matcher.find()) {
            String path = URLDecoder.decode(
                    matcher.group(1),
                    StandardCharsets.UTF_8
            );
            return resolveAbsoluteUrl(path);
        }

        for (Map.Entry<String, String> entry : BIZ_APPLICATION_URLS.entrySet()) {
            if (href.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return CATALOG_SEED_URL;
    }

    private String resolveAbsoluteUrl(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        if (path.startsWith("https://") || path.startsWith("http://")) {
            return path;
        }
        return BASE_URL + path.replaceFirst("^/", "");
    }

    private String resolveProjectCategory(String name, String sourceCategory) {
        String value = (name + " " + sourceCategory).replaceAll("\\s+", "");
        if (value.contains("펫")) {
            return "펫";
        }
        if (value.contains("여행") || value.contains("장기체류")) {
            return "여행자";
        }
        if (value.contains("자동차") || value.contains("운전자")
                || value.contains("이륜차") || value.contains("오토바이")) {
            return "운전자";
        }
        if (value.contains("치아")) {
            return "치아";
        }
        if (value.contains("건강") || value.contains("실손")
                || value.contains("암") || value.contains("자녀")
                || value.contains("간병") || value.contains("노후")) {
            return "건강·실비";
        }
        return "기타";
    }

    private String createFallbackDescription(MenuProduct product) {
        return product.insuranceName()
                + "의 주요 특징과 보장 내용을 확인하고 공식 상품 안내 페이지로 이동할 수 있습니다.";
    }

    private String cleanProductText(String text, String productName) {
        String normalized = normalizeText(text)
                .replace("(해당 특약 가입 시)", "")
                .replace("해당 특약 가입 시", "")
                .replaceAll("\\s+", " ")
                .trim();

        if (productName != null && !productName.isBlank()
                && normalized.equals(productName)) {
            return "";
        }
        return normalized;
    }

    private String cleanCoverageHeading(String text) {
        return normalizeText(text)
                .replaceFirst("^\\d{1,2}\\s*", "")
                .replace("(해당 특약 가입 시)", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isUsefulCoverageHeading(String value) {
        if (value == null || value.length() < 6 || value.length() > 200) {
            return false;
        }
        return !value.contains("보험료")
                && !value.contains("유의사항")
                && !value.contains("가입예시")
                && !value.contains("꼭 확인")
                && !value.contains("상품안내 및 보장내용");
    }

    private boolean isAllowedInsuranceImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(imageUrl);
            String host = uri.getHost();
            String lowerPath = uri.getPath().toLowerCase(Locale.ROOT);
            return host != null
                    && (host.equals("www.kbinsure.co.kr")
                    || host.endsWith(".kbinsure.co.kr"))
                    && lowerPath.contains("/images/ins_prdt/")
                    && !lowerPath.contains("/btn_")
                    && !lowerPath.contains("/ico_")
                    && !lowerPath.contains("logo");
        } catch (Exception e) {
            return false;
        }
    }

    private String resolveImageExtension(String originalName, String contentType) {
        String lower = originalName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return ".jpg";
        }
        if (lower.endsWith(".gif")) {
            return ".gif";
        }
        if (lower.endsWith(".webp")) {
            return ".webp";
        }
        if (lower.endsWith(".png")) {
            return ".png";
        }
        if (contentType.toLowerCase(Locale.ROOT).contains("jpeg")) {
            return ".jpg";
        }
        if (contentType.toLowerCase(Locale.ROOT).contains("gif")) {
            return ".gif";
        }
        if (contentType.toLowerCase(Locale.ROOT).contains("webp")) {
            return ".webp";
        }
        return ".png";
    }

    private String sanitizeFileName(String sourceCode) {
        return "kb_insurance_"
                + sourceCode.toLowerCase(Locale.ROOT)
                .replace(':', '_')
                .replaceAll("[^a-z0-9_-]", "_");
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.replaceAll("\\s+", " ").trim();
    }

    private String truncate(String text, int maxLength) {
        String normalized = normalizeText(text);
        return normalized.length() <= maxLength
                ? normalized
                : normalized.substring(0, maxLength);
    }

    private void logCatalogSummary(List<CrawledInsuranceProductDTO> products) {
        long imageCount = products.stream()
                .filter(product -> product.getInsuranceImage() != null
                        && !product.getInsuranceImage().isBlank())
                .count();
        int coverageCount = products.stream()
                .mapToInt(product -> product.getCoverages().size())
                .sum();
        Set<String> collectedSourceCodes = products.stream()
                .map(CrawledInsuranceProductDTO::getSourceCode)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        long matchCount = MATCH_RULES.stream()
                .filter(rule -> collectedSourceCodes.contains(rule.sourceCode()))
                .count();

        log.info("==================================================");
        log.info(
                "[보험 카탈로그] 수집 완료: 상품 {}건, 이미지 {}건, 보장 {}건, 추천매핑 {}건",
                products.size(),
                imageCount,
                coverageCount,
                matchCount
        );
        for (CrawledInsuranceProductDTO product : products) {
            log.info(
                    "   [{}] {} / {} / {}",
                    product.getSourceCode(),
                    product.getInsuranceName(),
                    product.getInsuranceCategory(),
                    product.getApplicationUrl()
            );
        }
        log.info("==================================================");
    }

    private record MenuProduct(
            String sourceCode,
            String insuranceName,
            String sourceCategory,
            String projectCategory,
            String applicationUrl
    ) {
    }

    private record InsuranceMatchRule(
            String sourceCode,
            String spendingCategoryName,
            String projectCategory,
            String recommendationReason,
            int priority
    ) {
    }
}
