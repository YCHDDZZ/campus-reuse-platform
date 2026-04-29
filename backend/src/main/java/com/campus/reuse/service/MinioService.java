package com.campus.reuse.service;

import com.campus.reuse.config.MinioProperties;
import com.campus.reuse.exception.AppException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MinioService {
    private static final Logger log = LoggerFactory.getLogger(MinioService.class);

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public MinioService(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    @PostConstruct
    public void initBucket() {
        ensureBucketExists();
    }

    public Map<String, Object> upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new AppException("Upload file must not be empty");
        }
        ensureBucketExists();
        String objectName = buildObjectName(folder, file.getOriginalFilename());
        String contentType = StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : "application/octet-stream";
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new AppException("Upload file to MinIO failed: " + e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bucket", minioProperties.getBucket());
        result.put("objectName", objectName);
        result.put("originalFilename", file.getOriginalFilename());
        result.put("contentType", contentType);
        result.put("size", file.getSize());
        result.put("url", buildPreviewUrl(objectName));
        return result;
    }

    public GetObjectResponse getObject(String objectName) {
        validateObjectName(objectName);
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new AppException("Read MinIO object failed: " + e.getMessage());
        }
    }

    public StatObjectResponse statObject(String objectName) {
        validateObjectName(objectName);
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new AppException("Read MinIO object metadata failed: " + e.getMessage());
        }
    }

    public void delete(String objectName) {
        validateObjectName(objectName);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new AppException("Delete MinIO object failed: " + e.getMessage());
        }
    }

    public String buildPreviewUrl(String objectName) {
        return "/api/files/preview?objectName=" + URLEncoder.encode(objectName, StandardCharsets.UTF_8);
    }

    private void ensureBucketExists() {
        String bucket = minioProperties.getBucket();
        if (!StringUtils.hasText(bucket)) {
            throw new AppException("MinIO bucket is not configured");
        }
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket: {}", bucket);
            }
        } catch (Exception e) {
            throw new AppException("Initialize MinIO bucket failed: " + e.getMessage());
        }
    }

    private String buildObjectName(String folder, String originalFilename) {
        String safeFolder = StringUtils.hasText(folder) ? folder.trim().replace("\\", "/") : "common";
        safeFolder = safeFolder.replaceAll("^/+", "").replaceAll("/+$", "");
        String cleanName = StringUtils.cleanPath(originalFilename == null ? "file" : originalFilename);
        String extension = "";
        int dotIndex = cleanName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = cleanName.substring(dotIndex);
        }
        LocalDate today = LocalDate.now();
        return safeFolder + "/"
                + today.getYear() + "/"
                + String.format("%02d", today.getMonthValue()) + "/"
                + String.format("%02d", today.getDayOfMonth()) + "/"
                + UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private void validateObjectName(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            throw new AppException("objectName must not be blank");
        }
    }
}
