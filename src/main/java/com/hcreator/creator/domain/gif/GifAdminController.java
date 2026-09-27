package com.hcreator.creator.domain.gif;

import com.hcreator.creator.common.S3Service;
import com.hcreator.creator.repository.gif.GifTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/gif")
@RequiredArgsConstructor
public class GifAdminController {

    private final GifTemplateRepository gifTemplateRepository;
    public record GifTemplateCreateRequest(String title, String imagePath, String detailContent) {}
    private final S3Service s3Service;

    // 1. 업로드용 presigned URL 요청
    @PostMapping("/upload-url")
    public S3Service.PresignedUploadResult getUploadUrl(@RequestBody Map<String, String> body) {
        String fileName = body.get("fileName");
        return s3Service.createUploadUrl("gif-templates", fileName);
    }

    // 2. S3 업로드 완료 후, DB에 최종 저장
    @PostMapping
    public GifTemplate create(@RequestBody GifTemplateCreateRequest request) {
        GifTemplate template = new GifTemplate();
        template.setTitle(request.title());
        template.setImagePath(request.imagePath());
        template.setDetailContent(request.detailContent());

        Integer maxOrder = gifTemplateRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(GifTemplate::getSortOrder)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        template.setSortOrder(maxOrder + 1);

        return gifTemplateRepository.save(template);
    }

    // 3. 목록 조회
    @GetMapping
    public List<GifTemplate> list() {
        return gifTemplateRepository.findAllByOrderBySortOrderAsc();
    }

    // 4. 삭제
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        GifTemplate template = gifTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("템플릿을 찾을 수 없습니다."));
        s3Service.deleteFile(extractFileKey(template.getImagePath()));
        gifTemplateRepository.deleteById(id);
    }

    private String extractFileKey(String publicUrl) {
        int idx = publicUrl.indexOf(".amazonaws.com/");
        return publicUrl.substring(idx + ".amazonaws.com/".length());
    }

    @PutMapping("/{id}")
    public GifTemplate update(@PathVariable Long id, @RequestBody GifTemplateCreateRequest request) {
        GifTemplate template = gifTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("템플릿을 찾을 수 없습니다."));
        template.setTitle(request.title());
        if (request.imagePath() != null && !request.imagePath().isBlank()) {
            template.setImagePath(request.imagePath());
        }
        template.setDetailContent(request.detailContent());
        return gifTemplateRepository.save(template);
    }

}