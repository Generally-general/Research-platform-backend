package com.research.platform.service;

import com.research.platform.event.FileUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentProcessor {

    private final Path root = Paths.get("uploads");

    @Async
    @EventListener
    public void handleFileUploadedEvent(FileUploadedEvent event) throws InterruptedException {
        String fileName = event.fileName();
        File file = root.resolve(fileName).toFile();

        try {
            log.info("Starting extraction for: {}", fileName);
            String extractedText = "";

            if(fileName.endsWith(".pdf")) {
                extractedText = extractFromPdf(file);
            } else {
                extractedText = extractUsingOcr(file);
            }

            log.info("Extraction Complete! Preview:\n{}...",
                    extractedText.substring(0, Math.min(extractedText.length(), 200)));
        } catch (Exception e) {
            log.error("Failed to process {}: {}", fileName, e.getMessage());
        }
    }

    private String extractFromPdf(File file) throws IOException {
        try(PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractUsingOcr(File file) throws TesseractException {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/tessdata");
        return tesseract.doOCR(file);
    }
}
