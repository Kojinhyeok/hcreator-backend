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
    private final com.hcreator.creator.domain.gif.FeaturedGifRepository featuredGifRepository;

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
        model.addAttribute("aiImageItems", portfolioItemRepository.findByCategoryOrderByCreatedAtDesc("ai-image"));

        List<PortfolioItem> cardNews = portfolioItemRepository.findByCategoryOrderByCreatedAtDesc("card-news");
        model.addAttribute("cardNewsBasic", cardNews.stream().filter(i -> !"square".equals(i.getCardNewsLayout())).toList());
        model.addAttribute("cardNewsSquare", cardNews.stream().filter(i -> "square".equals(i.getCardNewsLayout())).toList());

        model.addAttribute("detailPageItems", portfolioItemRepository.findByCategoryOrderByCreatedAtDesc("detail-page"));

        model.addAttribute("promoShort", portfolioVideoRepository.findByCategoryAndSubLabelOrderByCreatedAtDesc("product-promo", "숏폼형"));
        model.addAttribute("promoHorizontal", portfolioVideoRepository.findByCategoryAndSubLabelOrderByCreatedAtDesc("product-promo", "가로형"));
        model.addAttribute("aiDrama", portfolioVideoRepository.findByCategoryAndSubLabelOrderByCreatedAtDesc("ai-production", "AI 드라마"));
        model.addAttribute("aiAnimation", portfolioVideoRepository.findByCategoryAndSubLabelOrderByCreatedAtDesc("ai-production", "애니메이션"));
        model.addAttribute("aiAdLaw", portfolioVideoRepository.findByCategoryAndSubLabelOrderByCreatedAtDesc("ai-production", "표시광고법"));
        model.addAttribute("brandShort", portfolioVideoRepository.findByCategoryAndSubLabelOrderByCreatedAtDesc("brand", "숏폼형"));
        model.addAttribute("brandHorizontal", portfolioVideoRepository.findByCategoryAndSubLabelOrderByCreatedAtDesc("brand", "가로형"));
        model.addAttribute("viralShort", portfolioVideoRepository.findByCategoryAndSubLabelOrderByCreatedAtDesc("viral", "숏폼형"));
        model.addAttribute("reviewHorizontal", portfolioVideoRepository.findByCategoryAndSubLabelOrderByCreatedAtDesc("review", "가로형"));

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

    @GetMapping("/marketing/gif")
    public String marketingGif(Model model) {
        model.addAttribute("featuredGifTemplates", featuredGifRepository.findAllByOrderBySortOrderAsc());
        model.addAttribute("gifTemplates", gifTemplateRepository.findAllByOrderBySortOrderAsc());
        return "marketing-gif";
    }

}