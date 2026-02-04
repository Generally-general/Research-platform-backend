package com.research.platform.service;

import com.research.platform.event.FileUploadedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentProcessor {

    @Async
    @EventListener
    public void handleFileUploadedEvent(FileUploadedEvent event) throws InterruptedException {
        log.info("Started background processing for file: {}", event.fileName());

        Thread.sleep(5000);

        log.info("Successfully finished Phase 2 (Parsing) for: {}", event.fileName());
    }
}
