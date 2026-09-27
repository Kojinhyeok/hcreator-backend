package com.hcreator.creator.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    /**
     * 업로드용 presigned URL 발급.
     * folder 예: "gif-templates", "portfolio"
     * 반환: uploadUrl(프론트가 PUT할 주소), fileKey(저장할 최종 경로), publicUrl(완료 후 접근 URL)
     */
    public PresignedUploadResult createUploadUrl(String folder, String originalFileName) {
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFileName.substring(dotIndex);
        }
        String fileKey = folder + "/" + UUID.randomUUID() + extension;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

        String publicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fileKey);

        return new PresignedUploadResult(presigned.url().toString(), fileKey, publicUrl);
    }

    public void deleteFile(String fileKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build());
    }

    public record PresignedUploadResult(String uploadUrl, String fileKey, String publicUrl) {}
}