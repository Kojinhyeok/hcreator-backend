package com.hcreator.creator.domain.portfolio;

import com.hcreator.creator.common.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/portfolio")
@RequiredArgsConstructor
public class PortfolioAdminController {

    private final PortfolioItemRepository itemRepository;
    private final PortfolioVideoRepository videoRepository;
    private final S3Service s3Service;

    @PostMapping("/upload-url")
    public S3Service.PresignedUploadResult getUploadUrl(@RequestBody Map<String, String> body) {
        return s3Service.createUploadUrl("portfolio", body.get("fileName"));
    }

    // ===== 이미지 항목 (AI이미지/카드뉴스/상세페이지) =====

    public record ItemRequest(String category, String cardNewsLayout, String name, String imagePath, String linkUrl) {}

    @GetMapping("/items")
    public List<PortfolioItem> listItems(@RequestParam String category) {
        return itemRepository.findByCategoryOrderByCreatedAtDesc(category);
    }

    @PostMapping("/items")
    public PortfolioItem createItem(@RequestBody ItemRequest req) {
        PortfolioItem item = PortfolioItem.builder()
                .category(req.category())
                .cardNewsLayout(req.cardNewsLayout())
                .name(req.name())
                .imagePath(req.imagePath())
                .linkUrl(req.linkUrl())
                .build();
        return itemRepository.save(item);
    }

    @PutMapping("/items/{id}")
    public PortfolioItem updateItem(@PathVariable Long id, @RequestBody ItemRequest req) {
        PortfolioItem item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("항목을 찾을 수 없습니다."));
        item.setCategory(req.category());
        item.setCardNewsLayout(req.cardNewsLayout());
        item.setName(req.name());
        item.setLinkUrl(req.linkUrl());
        if (req.imagePath() != null && !req.imagePath().isBlank()) {
            item.setImagePath(req.imagePath());
        }
        return itemRepository.save(item);
    }

    @DeleteMapping("/items/{id}")
    public void deleteItem(@PathVariable Long id) {
        PortfolioItem item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("항목을 찾을 수 없습니다."));
        s3Service.deleteFile(extractFileKey(item.getImagePath()));
        itemRepository.deleteById(id);
    }

    // ===== 영상 =====

    public record VideoRequest(String category, String subLabel, String format, String title, String thumbnailPath, String linkUrl) {}

    @GetMapping("/videos")
    public List<PortfolioVideo> listVideos(@RequestParam String category, @RequestParam String subLabel) {
        return videoRepository.findByCategoryAndSubLabelOrderByCreatedAtDesc(category, subLabel);
    }

    @PostMapping("/videos")
    public PortfolioVideo createVideo(@RequestBody VideoRequest req) {
        PortfolioVideo video = PortfolioVideo.builder()
                .category(req.category()).subLabel(req.subLabel()).format(req.format())
                .title(req.title()).thumbnailPath(req.thumbnailPath()).linkUrl(req.linkUrl())
                .build();
        return videoRepository.save(video);
    }

    @PutMapping("/videos/{id}")
    public PortfolioVideo updateVideo(@PathVariable Long id, @RequestBody VideoRequest req) {
        PortfolioVideo video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("영상을 찾을 수 없습니다."));
        video.setCategory(req.category());
        video.setSubLabel(req.subLabel());
        video.setFormat(req.format());
        video.setTitle(req.title());
        video.setLinkUrl(req.linkUrl());
        if (req.thumbnailPath() != null && !req.thumbnailPath().isBlank()) {
            video.setThumbnailPath(req.thumbnailPath());
        }
        return videoRepository.save(video);
    }

    @DeleteMapping("/videos/{id}")
    public void deleteVideo(@PathVariable Long id) {
        PortfolioVideo video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("영상을 찾을 수 없습니다."));
        s3Service.deleteFile(extractFileKey(video.getThumbnailPath()));
        videoRepository.deleteById(id);
    }

    private String extractFileKey(String publicUrl) {
        if (publicUrl == null) return "";
        int idx = publicUrl.indexOf(".amazonaws.com/");
        return idx >= 0 ? publicUrl.substring(idx + ".amazonaws.com/".length()) : publicUrl;
    }
}