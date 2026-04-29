package com.campus.reuse.controller;

import com.campus.reuse.common.ApiResponse;
import com.campus.reuse.service.MinioService;
import io.minio.GetObjectResponse;
import io.minio.StatObjectResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final MinioService minioService;

    public FileController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(defaultValue = "common") String folder) {
        return ApiResponse.ok("File uploaded successfully", minioService.upload(file, folder));
    }

    @GetMapping("/preview")
    public ResponseEntity<InputStreamResource> preview(@RequestParam String objectName) {
        StatObjectResponse stat = minioService.statObject(objectName);
        GetObjectResponse object = minioService.getObject(objectName);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (stat.contentType() != null && !stat.contentType().isBlank()) {
            mediaType = MediaType.parseMediaType(stat.contentType());
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(stat.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + stat.object() + "\"")
                .body(new InputStreamResource(object));
    }

    @DeleteMapping
    public ApiResponse<Map<String, Object>> delete(@RequestParam String objectName) {
        minioService.delete(objectName);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("objectName", objectName);
        result.put("deleted", true);
        return ApiResponse.ok("File deleted successfully", result);
    }
}
