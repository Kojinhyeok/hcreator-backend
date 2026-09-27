package com.hcreator.creator.common;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class PublicUploadController {

    private final S3Service s3Service;

    @PostMapping("/presign")
    public S3Service.PresignedUploadResult presign(@RequestBody Map<String, String> body) {
        String folder = body.getOrDefault("folder", "uploads");
        return s3Service.createUploadUrl(folder, body.get("fileName"));
    }
}