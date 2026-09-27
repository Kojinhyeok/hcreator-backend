package com.hcreator.creator.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class PageController {

    /**
     * 헤더 네비게이션에서 현재 위치를 표시하기 위한 키.
     * 경로 접두사로 판별하므로 /marketing/gif 같은 하위 페이지도 자동으로 잡힌다.
     */
    @ModelAttribute("activeNav")
    public String activeNav(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/about")) return "about";
        if (path.startsWith("/clinical")) return "clinical";
        if (path.startsWith("/marketing")) return "marketing";
        if (path.startsWith("/voucher")) return "voucher";
        if (path.startsWith("/portfolio")) return "portfolio";
        if (path.startsWith("/support")) return "support";
        return "home";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/clinical")
    public String clinical() {
        return "redirect:https://www.humantest.co.kr/";
    }

    @GetMapping("/marketing")
    public String marketing() {
        return "marketing";
    }

    @GetMapping("/marketing/gif")
    public String marketingGif() {
        return "marketing-gif";
    }

    @GetMapping("/marketing/product")
    public String marketingProduct() {
        return "marketing-product";
    }

    @GetMapping("/marketing/detail-page")
    public String marketingDetailPage() {
        return "marketing-detail-page";
    }

    @GetMapping("/marketing/blog")
    public String marketingBlog() {
        return "marketing-blog";
    }

    @GetMapping("/marketing/video")
    public String marketingVideo() {
        return "marketing-video";
    }

    @GetMapping("/voucher")
    public String voucher() {
        return "voucher";
    }

    @GetMapping("/portfolio")
    public String portfolio() {
        return "portfolio";
    }

    @GetMapping("/support")
    public String support() {
        return "support";
    }

    @GetMapping("/support/apply")
    public String supportApply() {
        return "support-apply";
    }

    @GetMapping("/support/inquiry")
    public String supportInquiry() {
        return "support-inquiry";
    }
}