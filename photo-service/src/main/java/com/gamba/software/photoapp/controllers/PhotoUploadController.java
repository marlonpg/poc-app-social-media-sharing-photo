package com.gamba.software.photoapp.controllers;

import com.gamba.software.photoapp.services.StorageService;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/photos")
public class PhotoUploadController {

    private StorageService storageService;

    public PhotoUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadPhoto(
            @RequestParam("file") MultipartFile file) throws FileUploadException {

        String fileUrl = storageService.store(file);

        return ResponseEntity.ok(new FileUploadResponse(fileUrl));
    }
    public record FileUploadResponse(String fileUrl) {

    }
}
