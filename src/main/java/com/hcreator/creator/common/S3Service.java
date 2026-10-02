package com.hcreator.creator.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Value("${aws.cloudfront.domain}")
    private String cloudfrontDomain;

    private static final String GIFSICLE_PATH = "/usr/local/bin/gifsicle";

    /**
     * 업로드용 presigned URL 발급 (압축이 필요 없는 파일용, 기존 방식 그대로 유지).
     */
    public PresignedUploadResult createUploadUrl(String folder, String originalFileName) {
        String extension = extractExtension(originalFileName);
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
        String publicUrl = String.format("https://%s/%s", cloudfrontDomain, fileKey);

        return new PresignedUploadResult(presigned.url().toString(), fileKey, publicUrl);
    }

    /**
     * 파일을 서버에서 받아 GIF면 gifsicle로 압축한 뒤 S3에 업로드.
     * GIF가 아니면 원본 그대로 업로드.
     */
    public PresignedUploadResult uploadWithGifCompression(MultipartFile file, String folder) throws IOException {
        String extension = extractExtension(file.getOriginalFilename());
        String fileKey = folder + "/" + UUID.randomUUID() + extension;

        // 압축 기능 비활성화 — 원본 그대로 업로드
        byte[] uploadBytes = file.getBytes();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileKey)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(uploadBytes)
        );

        String publicUrl = String.format("https://%s/%s", cloudfrontDomain, fileKey);
        return new PresignedUploadResult(null, fileKey, publicUrl);
    }

    private byte[] compressGif(MultipartFile file) throws IOException {
        Path inputPath = Files.createTempFile("gif-in-", ".gif");
        Path outputPath = Files.createTempFile("gif-out-", ".gif");
        try {
            file.transferTo(inputPath);
            long originalSize = Files.size(inputPath);
    
            ProcessBuilder pb = new ProcessBuilder(
                    GIFSICLE_PATH, "--optimize=3", "--lossy=150", "--colors", "128",
                    "--resize-width", "800",
                    inputPath.toString(), "-o", outputPath.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
    
            String processOutput;
            try (InputStream is = process.getInputStream()) {
                processOutput = new String(is.readAllBytes());
            }
            int exitCode = process.waitFor();
    
            System.out.println(">>> gifsicle exitCode=" + exitCode + ", output=[" + processOutput + "]");
    
            if (exitCode == 0 && Files.size(outputPath) > 0) {
                long compressedSize = Files.size(outputPath);
                System.out.printf(">>> GIF 압축 성공: %d bytes -> %d bytes (%.1f%% 감소)%n",
                        originalSize, compressedSize, (1 - (double) compressedSize / originalSize) * 100);
                return Files.readAllBytes(outputPath);
            }
    
            System.out.println(">>> GIF 압축 실패, 원본 그대로 업로드함");
            return Files.readAllBytes(inputPath);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(">>> GIF 압축 중 인터럽트 발생: " + e.getMessage());
            return Files.readAllBytes(inputPath);
        } finally {
            Files.deleteIfExists(inputPath);
            Files.deleteIfExists(outputPath);
        }
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null) return "";
        int dotIndex = originalFileName.lastIndexOf('.');
        return dotIndex >= 0 ? originalFileName.substring(dotIndex) : "";
    }

    public void deleteFile(String fileKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build());
    }

    public record PresignedUploadResult(String uploadUrl, String fileKey, String publicUrl) {}
}