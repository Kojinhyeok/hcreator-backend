package com.hcreator.creator.domain.section;

import com.hcreator.creator.common.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/page-attachment")
@RequiredArgsConstructor
public class PageAttachmentAdminController {

    private final PageAttachmentRepository repository;
    private final S3Service s3Service;

    @PostMapping("/upload-url")
    public S3Service.PresignedUploadResult getUploadUrl(@RequestBody Map<String, String> body) {
        return s3Service.createUploadUrl("page-attachments", body.get("fileName"));
    }

    public record AttachmentRequest(String pageType, String label, String filePath, String fileName) {}

    @GetMapping
    public PageAttachment get(@RequestParam String pageType) {
        return repository.findByPageType(pageType).orElse(null);
    }

    @PostMapping
    public PageAttachment save(@RequestBody AttachmentRequest req) {
        PageAttachment attachment = repository.findByPageType(req.pageType()).orElseGet(PageAttachment::new);
        attachment.setPageType(req.pageType());
        attachment.setLabel(req.label());
        if (req.filePath() != null && !req.filePath().isBlank()) {
            attachment.setFilePath(req.filePath());
            attachment.setFileName(req.fileName());
        }
        return repository.save(attachment);
    }

    @DeleteMapping
    public void delete(@RequestParam String pageType) {
        repository.findByPageType(pageType).ifPresent(a -> {
            if (a.getFilePath() != null) {
                s3Service.deleteFile(extractFileKey(a.getFilePath()));
            }
            repository.delete(a);
        });
    }

    private String extractFileKey(String publicUrl) {
        int idx = publicUrl.indexOf(".amazonaws.com/");
        return idx >= 0 ? publicUrl.substring(idx + ".amazonaws.com/".length()) : publicUrl;
    }
}