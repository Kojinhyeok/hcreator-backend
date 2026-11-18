package com.hcreator.creator.domain.banner;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 각 페이지 배너/문구의 "기본값" (관리자가 바꾸기 전, 지금 사이트에 보이는 내용).
 * show* 값은 관리자 화면에서 어떤 입력칸을 보여줄지 정한다.
 */
public final class BannerDefaults {

    private BannerDefaults() {}

    public record Def(String key, String name,
                      String label, String title, String description,
                      String buttonText, String buttonLink, String button2Text, String button2Link,
                      String videoPath, String imagePath,
                      boolean showLabel, boolean showDescription,
                      boolean showButton, boolean showButton2,
                      boolean showVideo, boolean showImage) {}

    public static final Map<String, Def> ALL = new LinkedHashMap<>();

    private static void add(Def def) {
        ALL.put(def.key(), def);
    }

    static {
        add(new Def("index", "메인 - 상단 배너", "CLINICAL TO MARKET", "당신의 제품이 더 빛날 수 있도록", "임상부터 마케팅, 수출까지 원스톱으로 지원합니다.",
            "포트폴리오 보기", "/portfolio", null, null, "/videos/main-hero.mp4", null,
            true, true, true, false, true, false));
        add(new Def("index-cta", "메인 - 하단 문구 / 이미지", null, "증명된 데이터로 매출의 기회를 잡으세요.", "임상 결과를 콘텐츠와 마케팅으로 연결해, 실제 구매로 이어지는 흐름을 함께 만듭니다.",
            "포트폴리오 보기", "/portfolio", "문의하기", "/support", null, "/images/logo-vertical.png",
            false, true, true, true, false, true));
        add(new Def("about", "회사소개", "ABOUT", "임상시험부터 마케팅까지\n원스톱 뷰티 솔루션", "임상시험은 끝이 아니라, 매출의 시작입니다.",
            null, null, null, null, "/videos/main-hero.mp4", null,
            true, true, false, false, true, false));
        add(new Def("marketing", "마케팅 서비스", "MARKETING SERVICE", "임상 결과를, 팔리는 콘텐츠로", "GIF부터 상세페이지, 영상까지 필요한 콘텐츠를 한번에.",
            null, null, null, null, null, null,
            true, true, false, false, false, false));
        add(new Def("product", "제품 촬영", "P R O D U C T   V I S U A L", "제품 비주얼 제작", "제품을 정확하게 촬영하고, 브랜드의 이미지는 AI로 확장합니다.",
            null, null, null, null, "/videos/product-hero.mp4", null,
            true, true, false, false, true, false));
        add(new Def("blog", "블로그 마케팅", "B L O G   M A R K E T I N G", "블로그 마케팅", "아무것도 없는 블로그에서, 검색되는 브랜드로.\n제품 분석부터 키워드 선정, 콘텐츠 발행까지 블로그 마케팅의 시작을 함께합니다.",
            null, null, null, null, "/videos/blog-hero.mp4", null,
            true, true, false, false, true, false));
        add(new Def("gif", "임상 GIF", "G I F", "임상 GIF 제작", "복잡한 임상 데이터를 소비자가 한 눈에 이해할 수 있는 콘텐츠로 제작합니다.",
            null, null, null, null, "/videos/gif-hero.mp4", null,
            true, true, false, false, true, false));
        add(new Def("detail-page", "상세페이지", "D E T A I L   P A G E", "상세페이지 제작", "제품 특징부터 임상 결과까지, 제품의 매력이 잘 보이는 상세페이지를 만듭니다.",
            null, null, null, null, "/videos/detail-page-hero.mp4", null,
            true, true, false, false, true, false));
        add(new Def("video", "영상 제작", "V I D E O   C O N T E N T", "영상 제작", "최신 트렌드에 맞춘 영상 콘텐츠로, 브랜드와 제품을 더 효과적으로 보여줍니다.",
            null, null, null, null, "/videos/video-production-hero.mp4", null,
            true, true, false, false, true, false));
        add(new Def("portfolio", "포트폴리오", "PORTFOLIO", "포트폴리오", "AI 이미지, 카드뉴스, 상세페이지, 영상까지 — 휴먼이 만든 콘텐츠를 확인해보세요.",
            null, null, null, null, "/videos/portfolio-hero.mp4", null,
            true, true, false, false, true, false));
        add(new Def("voucher", "수출바우처", "EXPORT VOUCHER", "수출바우처", "수출바우처로 해외 진출에 필요한 콘텐츠를 제작하세요",
            null, null, null, null, "/videos/export-voucher-hero.mp4", null,
            true, true, false, false, true, false));
        add(new Def("support", "서비스 지원", "SUPPORT", "서비스 지원", "필요한 서비스를 선택해 주세요. 남겨주신 신청 및 문의는 확인 후 신속하게 답변드리겠습니다.",
            null, null, null, null, "/videos/support-hero.mp4", null,
            true, true, false, false, true, false));
        add(new Def("support-apply", "서비스 신청", "SERVICE APPLY", "서비스 신청", "임상시험 이후의 콘텐츠 제작부터 마케팅까지\n제품의 성과가 실제 구매로 이어질 수 있도록 함께합니다.",
            null, null, null, null, "/videos/support-apply-hero.mp4", null,
            true, true, false, false, true, false));
        add(new Def("support-inquiry", "서비스 문의", "INQUIRY", "서비스 문의", "궁금한 점을 남겨주시면 확인 후 빠르게 답변드리겠습니다.",
            null, null, null, null, "/videos/support-inquiry-hero.mp4", null,
            true, true, false, false, true, false));
    }
}