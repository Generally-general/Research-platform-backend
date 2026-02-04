package com.research.platform.controller;

import com.research.platform.event.FileUploadedEvent;
import com.research.platform.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/docs")
@RequiredArgsConstructor
public class IngestionController {
    private final IngestionService ingestionService;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(
            @RequestParam("file") MultipartFile file
    ) {
        String filename = ingestionService.saveFile(file);

        eventPublisher.publishEvent(new FileUploadedEvent(filename));

        return ResponseEntity.ok("File uploaded. Processing started in background for: " + filename);
    }
}
