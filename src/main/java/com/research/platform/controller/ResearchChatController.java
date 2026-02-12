package com.research.platform.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/research")
@RequiredArgsConstructor
public class ResearchChatController {
    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    public ResearchChatController(@Qualifier("openAiChatModel") ChatModel chatModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    private static final Logger log =
            LoggerFactory.getLogger(ResearchChatController.class);

    @GetMapping("/earning-call-summary")
    public Map<String, Object> analyzeEarnings(@RequestParam String fileName) {
        try {
            log.info("Checking analysis status for: [{}]", fileName);
            SearchRequest request = SearchRequest.query("management discussion, tone, guidance")
                    .withTopK(50);

            List<Document> allDocs = vectorStore.similaritySearch(request);

            if (allDocs.isEmpty()) {
                log.info("Vector store is empty. OCR/Embedding still in progress...");
                return Map.of(
                        "status", "processing",
                        "message", "Document is currently being processed by the OCR engine."
                );
            }

            List<Document> filteredDocs = allDocs.stream()
                    .filter(doc -> {
                        String source = (String) doc.getMetadata().get("source");
                        return source != null && source.trim().equalsIgnoreCase(fileName.trim());
                    })
                    .limit(15)
                    .toList();

            if (filteredDocs.isEmpty()) {
                return Map.of(
                        "status", "processing",
                        "message", "Awaiting vectorization for this specific file."
                );
            }

            log.info("Found {} chunks for {}. Generating report...", filteredDocs.size(), fileName);

            String context = filteredDocs.stream()
                    .map(Document::getContent)
                    .collect(Collectors.joining("\n\n---\n\n"));

            String systemPrompt = """
                    ROLE: Senior Equity Research Analyst.
                    TASK: Generate a Management Commentary Report based ONLY on the provided context.
                    CONSTRAINT: If info is missing, write "Not Disclosed". Use professional, cautious language.
                    
                    STRUCTURE:
                    1. SENTIMENT: [Optimistic/Cautious/Neutral/Pessimistic] + Evidence.
                    2. CONFIDENCE: [High/Medium/Low] based on specificity.
                    3. KEY POSITIVES: 3-5 bullet points.
                    4. KEY CONCERNS: 3-5 bullet points.
                    5. GUIDANCE: Revenue, margin, and capex outlook.
                    6. GROWTH INITIATIVES: 2-3 specific new projects.
                    
                    CONTEXT:
                    %s
                    """.formatted(context);

            String report = chatModel.call(systemPrompt);

            return Map.of(
                    "analyzedFile", fileName,
                    "report", report,
                    "status", "complete",
                    "metadata", Map.of(
                            "model", "Llama-3.3-70b",
                            "sourceCount", filteredDocs.size(),
                            "processingType", "Hybrid OCR"
                    )
            );
        } catch (Exception e) {
            log.error("CRITICAL ERROR in analyzeEarnings for file {}: ", fileName, e);
            return Map.of("error", e.getMessage(), "type", e.getClass().getSimpleName());
        }
    }
}
