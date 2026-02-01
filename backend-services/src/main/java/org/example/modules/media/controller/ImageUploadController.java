package org.example.modules.media.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/images")

public class ImageUploadController {

    private final String uploadDir;
    private final String publicBaseUrl;

    public ImageUploadController(
            @Value("${app.media.upload-dir}") String uploadDir,
            @Value("${app.media.public-base-url}") String publicBaseUrl) {
        this.uploadDir = uploadDir;
        this.publicBaseUrl = publicBaseUrl;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "No file selected"));
        }

        try {
            // 1. Generate unique filename to prevent overwrites
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // 2. Save file to disk
            Path path = Path.of(uploadDir, newFilename);
            Files.createDirectories(path.getParent()); // Ensure directory exists
            Files.write(path, file.getBytes());

            // 3. Generate the Public URL
            // This matches the pattern in your StaticResourceConfig: /images/**
            String publicUrl = publicBaseUrl + newFilename;

            return ResponseEntity.ok(Collections.singletonMap("url", publicUrl));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to upload image"));
        }
    }
}
