package com.hcreator.creator.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
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

    private static final String FFMPEG_PATH = "/usr/local/bin/ffmpeg";
    private static final String NICE_PATH = "/usr/bin/nice";
    private static final long CONVERT_TIMEOUT_SEC = 120;   // ffmpeg 1회 실행 제한 시간
    private static final long LOCK_WAIT_SEC = 120;         // 다른 변환이 끝나길 기다리는 최대 시간
    private static final String CACHE_CONTROL = "public, max-age=31536000, immutable"; // 파일명이 UUID라 변하지 않음

    // 동시에 변환은 1개만 (t3.small 보호)
    private final Semaphore convertLock = new Semaphore(1);

    /**
     * 업로드용 presigned URL 발급 (기존 방식 그대로 유지)
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
        return new PresignedUploadResult(presigned.url().toString(), fileKey, publicUrl(fileKey));
    }

    /**
     * 변환 없이 원본 그대로 업로드 (에디터 이미지 등)
     */
    public PresignedUploadResult uploadRaw(MultipartFile file, String folder) throws IOException {
        String extension = extractExtension(file.getOriginalFilename());
        String fileKey = folder + "/" + UUID.randomUUID() + extension;

        try (InputStream in = file.getInputStream()) {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileKey)
                            .contentType(file.getContentType())
                            .cacheControl(CACHE_CONTROL)
                            .build(),
                    RequestBody.fromInputStream(in, file.getSize())
            );
        }
        return new PresignedUploadResult(null, fileKey, publicUrl(fileKey));
    }

    /**
     * GIF면 ffmpeg로 MP4(+ 첫 프레임 포스터 jpg)로 변환해 업로드하고 MP4 주소를 돌려준다.
     * - 포스터 키 규칙: abc.mp4 -> abc_poster.jpg
     * - 변환 실패/타임아웃/대기 초과 시 원본 GIF를 그대로 업로드(서비스 중단 방지)
     * - GIF가 아니면 원본 그대로 업로드
     */
    public PresignedUploadResult uploadGifAsMp4(MultipartFile file, String folder) throws IOException {
        String extension = extractExtension(file.getOriginalFilename());
        if (!".gif".equalsIgnoreCase(extension)) {
            return uploadRaw(file, folder);
        }

        String baseKey = folder + "/" + UUID.randomUUID();
        Path in = Files.createTempFile("gif-in-", ".gif");
        Path mp4 = Files.createTempFile("gif-out-", ".mp4");
        Path poster = Files.createTempFile("gif-poster-", ".jpg");
        Path logFile = Files.createTempFile("ffmpeg-", ".log");

        try {
            file.transferTo(in);

            boolean converted = false;
            try {
                if (convertLock.tryAcquire(LOCK_WAIT_SEC, TimeUnit.SECONDS)) {
                    try {
                        converted = convertToMp4(in, mp4, poster, logFile);
                    } finally {
                        convertLock.release();
                    }
                } else {
                    log.warn("GIF 변환 대기 시간 초과 - 원본 GIF로 저장합니다.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (converted) {
                String mp4Key = baseKey + ".mp4";
                putFile(mp4Key, mp4, "video/mp4");
                putFile(baseKey + "_poster.jpg", poster, "image/jpeg");
                return new PresignedUploadResult(null, mp4Key, publicUrl(mp4Key));
            }

            // 실패 시 원본 GIF 그대로 저장
            String gifKey = baseKey + ".gif";
            putFile(gifKey, in, "image/gif");
            return new PresignedUploadResult(null, gifKey, publicUrl(gifKey));
        } finally {
            Files.deleteIfExists(in);
            Files.deleteIfExists(mp4);
            Files.deleteIfExists(poster);
            Files.deleteIfExists(logFile);
        }
    }

    private boolean convertToMp4(Path in, Path mp4, Path poster, Path logFile) {
        try {
            long start = System.currentTimeMillis();

            boolean ok = runFfmpeg(logFile, List.of(
                    NICE_PATH, "-n", "10",
                    FFMPEG_PATH, "-nostdin", "-y", "-loglevel", "error",
                    "-i", in.toString(),
                    "-vf", "scale=trunc(iw/2)*2:trunc(ih/2)*2",
                    "-c:v", "libx264", "-crf", "23", "-preset", "veryfast", "-threads", "2",
                    "-pix_fmt", "yuv420p", "-an", "-movflags", "+faststart",
                    mp4.toString()));
            if (!ok || Files.size(mp4) == 0) return false;

            ok = runFfmpeg(logFile, List.of(
                    FFMPEG_PATH, "-nostdin", "-y", "-loglevel", "error",
                    "-i", mp4.toString(),
                    "-frames:v", "1", "-update", "1", "-q:v", "3",
                    poster.toString()));
            if (!ok || Files.size(poster) == 0) return false;

            log.info("GIF→MP4 변환 완료: {}ms, {} → {} bytes",
                    System.currentTimeMillis() - start, Files.size(in), Files.size(mp4));
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("GIF→MP4 변환 중 예외: {}", e.getMessage());
            return false;
        }
    }

    private boolean runFfmpeg(Path logFile, List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .redirectOutput(logFile.toFile())
                .start();

        if (!process.waitFor(CONVERT_TIMEOUT_SEC, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            log.warn("ffmpeg 시간 초과({}초) - 중단했습니다.", CONVERT_TIMEOUT_SEC);
            return false;
        }
        if (process.exitValue() != 0) {
            String output = Files.readString(logFile);
            log.warn("ffmpeg 실패(exit={}): {}", process.exitValue(),
                    output.length() > 500 ? output.substring(0, 500) : output);
            return false;
        }
        return true;
    }

    private void putFile(String key, Path path, String contentType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .cacheControl(CACHE_CONTROL)
                        .build(),
                RequestBody.fromFile(path)
        );
    }

    /**
     * 파일 키 또는 공개 URL(CloudFront / S3 주소)을 받아 삭제한다.
     * MP4면 짝이 되는 포스터(_poster.jpg)도 함께 삭제한다.
     */
    public void deleteFile(String keyOrUrl) {
        String key = toKey(keyOrUrl);
        if (key == null || key.isBlank()) return;

        deleteKey(key);
        if (key.endsWith(".mp4")) {
            deleteKey(key.substring(0, key.length() - 4) + "_poster.jpg");
        }
    }

    private void deleteKey(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    private String toKey(String keyOrUrl) {
        if (keyOrUrl == null || keyOrUrl.isBlank()) return null;

        String cloudfrontPrefix = "https://" + cloudfrontDomain + "/";
        if (keyOrUrl.startsWith(cloudfrontPrefix)) {
            return keyOrUrl.substring(cloudfrontPrefix.length());
        }
        int idx = keyOrUrl.indexOf(".amazonaws.com/");
        if (idx >= 0) {
            return keyOrUrl.substring(idx + ".amazonaws.com/".length());
        }
        return keyOrUrl;
    }

    private String publicUrl(String fileKey) {
        return String.format("https://%s/%s", cloudfrontDomain, fileKey);
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null) return "";
        int dotIndex = originalFileName.lastIndexOf('.');
        return dotIndex >= 0 ? originalFileName.substring(dotIndex) : "";
    }

    public record PresignedUploadResult(String uploadUrl, String fileKey, String publicUrl) {}
}