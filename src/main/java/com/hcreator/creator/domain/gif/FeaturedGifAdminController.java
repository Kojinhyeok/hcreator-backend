package com.hcreator.creator.domain.gif;

import com.hcreator.creator.common.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/featured-gif")
@RequiredArgsConstructor
public class FeaturedGifAdminController {

    private final FeaturedGifRepository featuredGifRepository;
    private final S3Service s3Service;

    public record FeaturedGifRequest(String title, String imagePath) {}

    // GIF는 MP4 + 포스터로 변환되어 저장됨
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public S3Service.PresignedUploadResult upload(@RequestParam("file") MultipartFile file) throws java.io.IOException {
        return s3Service.uploadGifAsMp4(file, "featured-gif");
    }

    @GetMapping
    public List<FeaturedGif> list() {
        return featuredGifRepository.findAllByOrderBySortOrderAsc();
    }

    @PostMapping
    public FeaturedGif create(@RequestBody FeaturedGifRequest request) {
        FeaturedGif gif = new FeaturedGif();
        gif.setTitle(request.title());
        gif.setImagePath(request.imagePath());

        Integer maxOrder = featuredGifRepository.findAllByOrderBySortOrderAsc().stream()
                .map(FeaturedGif::getSortOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        gif.setSortOrder(maxOrder + 1);

        return featuredGifRepository.save(gif);
    }

    @PutMapping("/{id}")
    public FeaturedGif update(@PathVariable Long id, @RequestBody FeaturedGifRequest request) {
        FeaturedGif gif = featuredGifRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("항목을 찾을 수 없습니다."));
        gif.setTitle(request.title());
        if (request.imagePath() != null && !request.imagePath().isBlank()) {
            gif.setImagePath(request.imagePath());
        }
        return featuredGifRepository.save(gif);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        FeaturedGif gif = featuredGifRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("항목을 찾을 수 없습니다."));
        s3Service.deleteFile(gif.getImagePath());
        featuredGifRepository.deleteById(id);
    }
}