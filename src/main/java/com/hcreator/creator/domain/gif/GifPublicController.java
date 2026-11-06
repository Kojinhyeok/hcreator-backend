package com.hcreator.creator.domain.gif;

import lombok.RequiredArgsConstructor;
import com.hcreator.creator.repository.gif.GifTemplateRepository;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/gif")
@RequiredArgsConstructor
public class GifPublicController {

    private final GifTemplateRepository gifTemplateRepository;

    @GetMapping("/{id}")
    public GifTemplate detail(@PathVariable Long id) {
        return gifTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("템플릿을 찾을 수 없습니다."));
    }

    /**
     * 템플릿 검색: 제목 + 상세 내용 안의 "글자"에서 찾는다.
     * (이미지 주소 등 HTML 태그 안의 문자열은 검색 대상이 아니다.)
     * 일치하는 템플릿 id 목록을 반환한다.
     */
    @GetMapping("/search")
    public List<Long> search(@RequestParam(name = "q", defaultValue = "") String q) {
        String keyword = normalize(q);
        List<Long> result = new ArrayList<>();
        if (keyword.isEmpty()) return result;

        for (GifTemplate t : gifTemplateRepository.findAllByOrderBySortOrderAsc()) {
            String haystack = normalize(t.getTitle()) + " " + normalize(htmlToText(t.getDetailContent()));
            if (haystack.contains(keyword)) {
                result.add(t.getId());
            }
        }
        return result;
    }

    private static String htmlToText(String html) {
        if (html == null) return "";
        String text = html
                .replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", " ")
                .replaceAll("(?s)<[^>]*>", " ");
        return text
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&amp;", "&");
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
    }
}