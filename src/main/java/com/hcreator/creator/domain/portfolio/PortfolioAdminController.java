package com.hcreator.creator.domain.portfolio;

import com.hcreator.creator.common.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/portfolio")
@RequiredArgsConstructor
public class PortfolioAdminController {

    private final PortfolioItemRepository itemRepository;
    private final PortfolioVideoRepository videoRepository;
    private final S3Service s3Service;

    // 공통 업로드 URL 발급 (이미지 항목 / 영상 썸네일 둘 다 여기 사용)
    @PostMapping("/upload-url")
    public S3Service.PresignedUploadResult getUploadUrl(@RequestBody Map<String, String> body) {
        return s3Service.createUploadUrl("portfolio", body.get("fileName"));
    }

    // ===== 세로형 이미지 아이템 =====

    public record ItemRequest(String category, String name, String imagePath, String linkUrl) {}

    @GetMapping("/items")
    public List<PortfolioItem> listItems() {
        return itemRepository.findAllByOrderBySortOrderAsc();
    }

    @PostMapping("/items")
    public PortfolioItem createItem(@RequestBody ItemRequest req) {
        Integer maxOrder = itemRepository.findAllByOrderBySortOrderAsc().stream()
                .map(PortfolioItem::getSortOrder).filter(Objects::nonNull)
                .max(Integer::compareTo).orElse(0);

        PortfolioItem item = PortfolioItem.builder()
                .category(req.category()).name(req.name())
                .imagePath(req.imagePath()).linkUrl(req.linkUrl())
                .sortOrder(maxOrder + 1)
                .build();
        return itemRepository.save(item);
    }

    @PutMapping("/items/{id}")
    public PortfolioItem updateItem(@PathVariable Long id, @RequestBody ItemRequest req) {
        PortfolioItem item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("항목을 찾을 수 없습니다."));
        item.setCategory(req.category());
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

    public record VideoRequest(String groupType, String title, String thumbnailPath, String linkUrl) {}

    @GetMapping("/videos")
    public List<PortfolioVideo> listVideos() {
        return videoRepository.findAllByOrderBySortOrderAsc();
    }

    @PostMapping("/videos")
    public PortfolioVideo createVideo(@RequestBody VideoRequest req) {
        Integer maxOrder = videoRepository.findAllByOrderBySortOrderAsc().stream()
                .map(PortfolioVideo::getSortOrder).filter(Objects::nonNull)
                .max(Integer::compareTo).orElse(0);

        PortfolioVideo video = PortfolioVideo.builder()
                .groupType(req.groupType()).title(req.title())
                .thumbnailPath(req.thumbnailPath()).linkUrl(req.linkUrl())
                .sortOrder(maxOrder + 1)
                .build();
        return videoRepository.save(video);
    }

    @PutMapping("/videos/{id}")
    public PortfolioVideo updateVideo(@PathVariable Long id, @RequestBody VideoRequest req) {
        PortfolioVideo video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("영상을 찾을 수 없습니다."));
        video.setGroupType(req.groupType());
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