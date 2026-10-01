package com.hcreator.creator.controller;

import com.hcreator.creator.domain.gif.GifTemplate;
import com.hcreator.creator.repository.gif.GifTemplateRepository;
import com.hcreator.creator.domain.portfolio.PortfolioItem;
import com.hcreator.creator.domain.portfolio.PortfolioItemRepository;
import com.hcreator.creator.domain.portfolio.PortfolioVideo;
import com.hcreator.creator.domain.portfolio.PortfolioVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final GifTemplateRepository gifTemplateRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final PortfolioVideoRepository portfolioVideoRepository;
    private final com.hcreator.creator.domain.section.ServiceDetailSectionRepository serviceDetailSectionRepository;
    private final com.hcreator.creator.domain.section.PageAttachmentRepository pageAttachmentRepository;

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
    public String marketingGif(Model model) {
        model.addAttribute("featuredGifTemplates", gifTemplateRepository.findByFeaturedTrueOrderBySortOrderAsc());
        model.addAttribute("gifTemplates", gifTemplateRepository.findByFeaturedFalseOrderBySortOrderAsc());
        return "marketing-gif";
    }

    @GetMapping("/marketing/product")
    public String marketingProduct(Model model) {
        model.addAttribute("sections", serviceDetailSectionRepository.findByPageTypeOrderBySortOrderAsc("product"));
        return "marketing-product";
    }

    @GetMapping("/marketing/detail-page")
    public String marketingDetailPage(Model model) {
        model.addAttribute("sections", serviceDetailSectionRepository.findByPageTypeOrderBySortOrderAsc("detail-page"));
        model.addAttribute("attachment", pageAttachmentRepository.findByPageType("detail-page").orElse(null));
        return "marketing-detail-page";
    }

    @GetMapping("/marketing/blog")
    public String marketingBlog(Model model) {
        model.addAttribute("sections", serviceDetailSectionRepository.findByPageTypeOrderBySortOrderAsc("blog"));
        return "marketing-blog";
    }

    @GetMapping("/marketing/video")
    public String marketingVideo(Model model) {
        model.addAttribute("sections", serviceDetailSectionRepository.findByPageTypeOrderBySortOrderAsc("video"));
        return "marketing-video";
    }

    @GetMapping("/voucher")
    public String voucher() {
        return "voucher";
    }

    @GetMapping("/portfolio")
    public String portfolio(Model model) {
        List<PortfolioItem> items = portfolioItemRepository.findAllByOrderBySortOrderAsc();
        List<PortfolioVideo> videos = portfolioVideoRepository.findAllByOrderBySortOrderAsc();

        // 영상은 그룹별로 나눠서 넘김 (product-intro / review / shortform / ai-production)
        model.addAttribute("portfolioItems", items);
        model.addAttribute("productIntroVideos", filterByGroup(videos, "product-intro"));
        model.addAttribute("reviewVideos", filterByGroup(videos, "review"));
        model.addAttribute("shortformVideos", filterByGroup(videos, "shortform"));
        model.addAttribute("aiProductionVideos", filterByGroup(videos, "ai-production"));

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

    @GetMapping("/admin")
    public String adminRedirect() {
        return "redirect:/admin/index.html";
    }

    private List<PortfolioVideo> filterByGroup(List<PortfolioVideo> videos, String group) {
        return videos.stream()
                .filter(v -> group.equals(v.getGroupType()))
                .collect(Collectors.toList());
    }
}