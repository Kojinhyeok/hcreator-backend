package com.hcreator.creator.domain.banner;

import com.hcreator.creator.domain.banner.BannerDefaults.Def;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final PageBannerRepository repository;

    public record BannerRequest(String label, String title, String description,
                                String buttonText, String buttonLink, String button2Text, String button2Link,
                                String videoPath, String imagePath) {}

    // ===== 공개 페이지용 =====

    /** 모든 페이지의 (기본값 + 관리자 변경값) */
    @Transactional(readOnly = true)
    public Map<String, BannerView> allViews() {
        Map<String, PageBanner> rows = rowsByKey();
        Map<String, BannerView> result = new LinkedHashMap<>();
        for (Def def : BannerDefaults.ALL.values()) {
            result.put(def.key(), toView(def, rows.get(def.key())));
        }
        return result;
    }

    // ===== 관리자 화면용 =====

    @Transactional(readOnly = true)
    public List<Map<String, Object>> adminList() {
        Map<String, PageBanner> rows = rowsByKey();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Def def : BannerDefaults.ALL.values()) {
            PageBanner row = rows.get(def.key());
            BannerView v = toView(def, row);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("key", def.key());
            m.put("name", def.name());
            m.put("customized", row != null);
            m.put("showLabel", def.showLabel());
            m.put("showDescription", def.showDescription());
            m.put("showButton", def.showButton());
            m.put("showButton2", def.showButton2());
            m.put("showVideo", def.showVideo());
            m.put("showImage", def.showImage());
            m.put("label", nz(v.getLabel()));
            m.put("title", nz(v.getTitle()));
            m.put("description", nz(v.getDescription()));
            m.put("buttonText", nz(v.getButtonText()));
            m.put("buttonLink", nz(v.getButtonLink()));
            m.put("button2Text", nz(v.getButton2Text()));
            m.put("button2Link", nz(v.getButton2Link()));
            m.put("videoPath", nz(v.getVideoUrl()));
            m.put("imagePath", nz(v.getImageUrl()));
            list.add(m);
        }
        return list;
    }

    @Transactional
    public void save(String key, BannerRequest req) {
        Def def = BannerDefaults.ALL.get(key);
        if (def == null) throw new IllegalArgumentException("알 수 없는 페이지입니다.");

        PageBanner row = repository.findByPageKey(key).orElseGet(() -> PageBanner.builder().pageKey(key).build());
        if (def.showLabel()) row.setLabel(text(req.label(), 200));
        row.setTitle(text(req.title(), 500));
        if (def.showDescription()) row.setDescription(text(req.description(), 1000));
        if (def.showButton()) {
            row.setButtonText(text(req.buttonText(), 100));
            row.setButtonLink(link(req.buttonLink()));
        }
        if (def.showButton2()) {
            row.setButton2Text(text(req.button2Text(), 100));
            row.setButton2Link(link(req.button2Link()));
        }
        if (def.showVideo()) row.setVideoPath(mediaPath(req.videoPath()));
        if (def.showImage()) row.setImagePath(mediaPath(req.imagePath()));
        repository.save(row);
    }

    /** 기본값으로 되돌리기 */
    @Transactional
    public void reset(String key) {
        repository.findByPageKey(key).ifPresent(repository::delete);
    }

    // ===== 내부 =====

    private Map<String, PageBanner> rowsByKey() {
        Map<String, PageBanner> rows = new HashMap<>();
        for (PageBanner b : repository.findAll()) rows.put(b.getPageKey(), b);
        return rows;
    }

    private BannerView toView(Def def, PageBanner row) {
        String title = pick(row == null ? null : row.getTitle(), def.title());
        String description = pick(row == null ? null : row.getDescription(), def.description());
        return BannerView.builder()
                .key(def.key())
                .label(pick(row == null ? null : row.getLabel(), def.label()))
                .title(title)
                .titleHtml(toHtml(title))
                .description(description)
                .descriptionHtml(toHtml(description))
                .buttonText(pick(row == null ? null : row.getButtonText(), def.buttonText()))
                .buttonLink(pick(row == null ? null : row.getButtonLink(), def.buttonLink()))
                .button2Text(pick(row == null ? null : row.getButton2Text(), def.button2Text()))
                .button2Link(pick(row == null ? null : row.getButton2Link(), def.button2Link()))
                .videoUrl(effectiveVideo(row, def))
                .imageUrl(pick(row == null ? null : row.getImagePath(), def.imagePath()))
                .build();
    }

    /** 관리자가 바꾼 값이 있으면 그 값, 없으면(null) 기본값 */
    private static String pick(String stored, String def) {
        return stored != null ? stored : (def == null ? "" : def);
    }

    /** 기본 영상은 실제 파일이 있을 때만 사용한다. (파일이 없으면 배너만 보인다) */
    private String effectiveVideo(PageBanner row, Def def) {
        if (row != null && row.getVideoPath() != null) return row.getVideoPath();
        String path = def.videoPath();
        if (path != null && !path.isBlank() && new ClassPathResource("static" + path).exists()) return path;
        return "";
    }

    private static String toHtml(String s) {
        if (s == null) return "";
        return HtmlUtils.htmlEscape(s).replace("\r\n", "\n").replace("\n", "<br>");
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String text(String s, int max) {
        if (s == null) return "";
        String t = s.replace("\r\n", "\n").trim();
        return t.length() > max ? t.substring(0, max) : t;
    }

    /** 버튼 링크: 내부 경로(/...) 또는 http(s)/mailto/tel 만 허용 */
    private static String link(String s) {
        String t = text(s, 500);
        if (t.isEmpty()) return "";
        String lower = t.toLowerCase();
        if (t.startsWith("/") && !t.startsWith("//")) return t;
        if (lower.startsWith("https://") || lower.startsWith("http://") || lower.startsWith("mailto:") || lower.startsWith("tel:")) return t;
        throw new IllegalArgumentException("링크는 /경로 또는 https:// 로 시작해야 합니다.");
    }

    /** 영상/이미지 주소: /경로 또는 https:// 만 허용, 빈 값은 "없음" */
    private static String mediaPath(String s) {
        String t = text(s, 1000);
        if (t.isEmpty()) return "";
        if (t.startsWith("/") && !t.startsWith("//")) return t;
        if (t.toLowerCase().startsWith("https://")) return t;
        throw new IllegalArgumentException("파일 주소가 올바르지 않습니다.");
    }
}