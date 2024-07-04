package com.example.cloud_story_be.controller;

import com.example.cloud_story_be.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileStorageService fileStorageService;
    private final ServletContext servletContext;

    @Autowired
    public FileController(FileStorageService fileStorageService, ServletContext servletContext) {
        this.fileStorageService = fileStorageService;
        this.servletContext = servletContext;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        logger.info("Uploading file: {}", file.getOriginalFilename());
        String fileName = fileStorageService.storeFile(file);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/uploads/")
                .path(fileName)
                .toUriString();

        logger.info("File uploaded successfully: {}", fileName);
        return ResponseEntity.ok(Map.of(
                "fileName", fileName,
                "fileDownloadUri", fileDownloadUri,
                "fileType", file.getContentType(),
                "size", file.getSize()
        ));
    }

    @GetMapping("/uploads/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        logger.info("Downloading file: {}", fileName);
        Resource resource;
        Path filePath;

        try {
            filePath = fileStorageService.getFilePath(fileName);
            resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                logger.error("File not found: {}", fileName);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            logger.error("Error while loading file: {}", fileName, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        String contentType = determineContentType(fileName, filePath);
        logger.info("Determined content type for {}: {}", fileName, contentType);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }

    private String determineContentType(String fileName, Path filePath) {
        String contentType = null;

        // 1. Try to probe content type
        try {
            contentType = Files.probeContentType(filePath);
            logger.info("Probed content type for {}: {}", fileName, contentType);
        } catch (IOException e) {
            logger.warn("Failed to probe content type for {}", fileName, e);
        }

        // 2. If probing fails, try to get MIME type from ServletContext
        if (StringUtils.isEmpty(contentType)) {
            contentType = servletContext.getMimeType(fileName);
            logger.info("Servlet context MIME type for {}: {}", fileName, contentType);
        }

        // 3. If still empty, determine by file extension
        if (StringUtils.isEmpty(contentType)) {
            contentType = getContentTypeByFileExtension(fileName);
            logger.info("Determined content type by extension for {}: {}", fileName, contentType);
        }

        // 4. If all else fails, use default
        if (StringUtils.isEmpty(contentType)) {
            contentType = "application/octet-stream";
            logger.info("Using default content type for {}: {}", fileName, contentType);
        }

        return contentType;
    }

    private String getContentTypeByFileExtension(String fileName) {
        String extension = StringUtils.getFilenameExtension(fileName);
        if (extension == null) {
            return null;
        }
        switch (extension.toLowerCase()) {
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "txt":
                return "text/plain";
            // Add more cases as needed
            default:
                return null;
        }
    }
}