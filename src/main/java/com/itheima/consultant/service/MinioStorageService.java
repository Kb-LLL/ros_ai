package com.itheima.consultant.service;

import com.itheima.consultant.dto.UploadedImageAsset;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class MinioStorageService {

    private static final DateTimeFormatter PATH_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioStorageService(@Value("${app.minio.endpoint}") String endpoint,
                               @Value("${app.minio.access-key}") String accessKey,
                               @Value("${app.minio.secret-key}") String secretKey,
                               @Value("${app.minio.bucket-name}") String bucketName) {
        this.bucketName = bucketName;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @PostConstruct
    public void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to initialize MinIO bucket", exception);
        }
    }

    public String uploadKnowledgeFile(MultipartFile file, Long userId, Long documentId, String fileName) {
        String objectKey = buildKnowledgeObjectKey(userId, documentId, fileName);
        putObject(file, objectKey);
        return objectKey;
    }

    public UploadedImageAsset uploadChatImage(MultipartFile file, Long userId) {
        String objectKey = buildChatImageObjectKey(userId, file.getOriginalFilename());
        putObject(file, objectKey);

        UploadedImageAsset asset = new UploadedImageAsset();
        asset.setObjectKey(objectKey);
        asset.setFileName(file.getOriginalFilename());
        asset.setContentType(normalizeContentType(file.getContentType()));
        asset.setFileSize(file.getSize());
        asset.setStorageType("minio");
        return asset;
    }

    public StoredObject readObject(String objectKey) {
        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectKey)
                .build())) {
            byte[] bytes = inputStream.readAllBytes();
            String contentType = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build()).contentType();
            return new StoredObject(bytes, normalizeContentType(contentType));
        } catch (Exception exception) {
            throw new RuntimeException("Failed to read object from MinIO", exception);
        }
    }

    public void deleteObjectQuietly(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build());
        } catch (Exception ignored) {
        }
    }

    public boolean isUserChatImage(String objectKey, Long userId) {
        return objectKey != null && objectKey.startsWith("chat-images/" + userId + "/");
    }

    public void deleteChatImage(String objectKey, Long userId) {
        if (!isUserChatImage(objectKey, userId)) {
            throw new RuntimeException("Image asset does not belong to current user");
        }
        deleteObjectQuietly(objectKey);
    }

    public String buildKnowledgeObjectKey(Long userId, Long documentId, String fileName) {
        return "knowledge/" + userId + "/" + documentId + "/" + sanitizeFileName(fileName);
    }

    private String buildChatImageObjectKey(Long userId, String fileName) {
        return "chat-images/" + userId + "/"
                + PATH_TIME_FORMATTER.format(LocalDateTime.now()) + "/"
                + UUID.randomUUID() + "-" + sanitizeFileName(fileName);
    }

    private void putObject(MultipartFile file, String objectKey) {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(normalizeContentType(file.getContentType()))
                    .build());
        } catch (Exception exception) {
            throw new RuntimeException("Failed to upload file to MinIO", exception);
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }
        return contentType;
    }

    public record StoredObject(byte[] bytes, String contentType) {
    }
}
