package com.hcreator.creator.domain.section;

import com.hcreator.creator.common.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/service-section")
@RequiredArgsConstructor
public class ServiceSectionAdminController {

    private final ServiceDetailSectionRepository repository;
    private final S3Service s3Service;

    @PostMapping("/upload-url")
    public S3Service.PresignedUploadResult getUploadUrl(@RequestBody Map<String, String> body) {
        return s3Service.createUploadUrl("service-sections", body.get("fileName"));
    }

    public record SectionRequest(String pageType, String title, String content, String imagePath) {}

    @GetMapping
    public List<ServiceDetailSection> list(@RequestParam String pageType) {
        return repository.findByPageTypeOrderBySortOrderAsc(pageType);
    }

    @PostMapping
    public ServiceDetailSection create(@RequestBody SectionRequest req) {
        Integer maxOrder = repository.findByPageTypeOrderBySortOrderAsc(req.pageType()).stream()
                .map(ServiceDetailSection::getSortOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        ServiceDetailSection section = new ServiceDetailSection();
        section.setPageType(req.pageType());
        section.setTitle(req.title());
        section.setContent(req.content());
        section.setImagePath(req.imagePath());
        section.setSortOrder(maxOrder + 1);
        return repository.save(section);
    }

    @PutMapping("/{id}")
    public ServiceDetailSection update(@PathVariable Long id, @RequestBody SectionRequest req) {
        ServiceDetailSection section = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("항목을 찾을 수 없습니다."));
        section.setTitle(req.title());
        section.setContent(req.content());
        if (req.imagePath() != null && !req.imagePath().isBlank()) {
            section.setImagePath(req.imagePath());
        }
        return repository.save(section);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        ServiceDetailSection section = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("항목을 찾을 수 없습니다."));
        if (section.getImagePath() != null) {
            s3Service.deleteFile(extractFileKey(section.getImagePath()));
        }
        repository.deleteById(id);
    }

    private String extractFileKey(String publicUrl) {
        int idx = publicUrl.indexOf(".amazonaws.com/");
        return idx >= 0 ? publicUrl.substring(idx + ".amazonaws.com/".length()) : publicUrl;
    }
}