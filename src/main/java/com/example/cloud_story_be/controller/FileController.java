package com.example.cloud_story_be.controller;

import com.example.cloud_story_be.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.ServletContext;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final ServletContext servletContext;

    @Autowired
    public FileController(FileStorageService fileStorageService, ServletContext servletContext) {
        this.fileStorageService = fileStorageService;
        this.servletContext = servletContext;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(fileName)
                .toUriString();

        return ResponseEntity.ok(Map.of(
                "fileName", fileName,
                "fileDownloadUri", fileDownloadUri,
                "fileType", file.getContentType(),
                "size", file.getSize()
        ));
    }

    @GetMapping("/uploads/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("fileName") String fileName) {
        Resource resource;
        try {
            resource = loadFileAsResource(fileName);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        // 파일의 MIME 타입을 결정
        String contentType = null;
        try {
            Path filePath = resource.getFile().toPath();
            contentType = Files.probeContentType(filePath);

            if (StringUtils.isEmpty(contentType)) {
                contentType = servletContext.getMimeType(resource.getFilename());
            }

            if (StringUtils.isEmpty(contentType)) {
                contentType = "application/octet-stream";
            }
        } catch (Exception ex) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    private Resource loadFileAsResource(String fileName) {
        try {
            // 업로드 폴더의 경로를 설정합니다
            Path filePath = fileStorageService.getFilePath(fileName);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Error: " + ex.getMessage());
        }
    }
}
