package com.research.platform.service;

import com.research.platform.event.FileUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentProcessor {

    private final Path root = Paths.get("uploads");
    private final VectorStore vectorStore;

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

            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> chunks = splitter.apply(List.of(new Document(extractedText)));

            chunks.forEach(chunk -> chunk.getMetadata().put("source", event.fileName()));

            log.info("Creating embeddings and saving {} chunks to Vector Store...", chunks.size());
            vectorStore.add(chunks);

            ((SimpleVectorStore) vectorStore).save(new File("vectorstore.json"));

            log.info("Phase 3 complete: Document is now searchable");
        } catch (Exception e) {
            log.error("Failed to process {}: {}", fileName, e.getMessage());
        }


    }

    private String extractFromPdf(File file) throws IOException {
        try(PDDocument document = org.apache.pdfbox.Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if(text == null || text.trim().isEmpty()) {
                log.info("PDF contains no selectable text. Falling back to OCR...");
                return extractUsingOcr(file);
            }
            return text;
        } catch (TesseractException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractUsingOcr(File file) throws TesseractException {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/tessdata");
        return tesseract.doOCR(file);
    }
}
