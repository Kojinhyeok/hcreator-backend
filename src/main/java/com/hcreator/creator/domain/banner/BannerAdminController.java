package com.hcreator.creator.domain.banner;

import com.hcreator.creator.common.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/banner")
@RequiredArgsConstructor
public class BannerAdminController {

    private final BannerService bannerService;
    private final S3Service s3Service;

    @GetMapping
    public List<Map<String, Object>> list() {
        return bannerService.adminList();
    }

    @PutMapping("/{key}")
    public Map<String, Object> save(@PathVariable String key, @RequestBody BannerService.BannerRequest req) {
        bannerService.save(key, req);
        return Map.of("ok", true);
    }

    @DeleteMapping("/{key}")
    public Map<String, Object> reset(@PathVariable String key) {
        bannerService.reset(key);
        return Map.of("ok", true);
    }

    /** 배너 영상 업로드: 서버가 웹용으로 압축(최대 가로 1280px, 소리 제거)해서 저장한다. */
    @PostMapping(value = "/upload-video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public S3Service.PresignedUploadResult uploadVideo(@RequestParam("file") MultipartFile file) throws IOException {
        return s3Service.uploadVideoCompressed(file, "banners", false);
    }

    /** 이미지 업로드 (메인 하단 이미지 등) */
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public S3Service.PresignedUploadResult uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String type = file.getContentType();
        if (type == null || !type.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 올릴 수 있습니다.");
        }
        return s3Service.uploadRaw(file, "banners");
    }
}