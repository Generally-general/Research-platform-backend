package com.research.platform.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
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

    private static final Logger log =
            LoggerFactory.getLogger(ResearchChatController.class);

    @GetMapping("/earning-call-summary")
    public Map<String, Object> analyzeEarnings(@RequestParam String fileName) {
        try {
            log.info("Searching for chunks related to file: {}", fileName);
            SearchRequest request = SearchRequest.query("management discussion, tone, revenue guidance, growth initiatives, challenges")
                .withTopK(10);

            List<Document> allDocs = vectorStore.similaritySearch(request);

            List<Document> filteredDocs = allDocs.stream()
                    .filter(doc -> fileName.equals(doc.getMetadata().get("source")))
                    .limit(10) // Take the top 10 relevant to this file
                    .toList();

            if (filteredDocs.isEmpty()) {
                return Map.of("error", "No data found for: " + fileName + ". Check if OCR produced text.");
            }

            String context = filteredDocs.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

            String systemPrompt = """
                        ROLE: Senior Equity Research Analyst.
                                    TASK: Generate a Management Commentary Report based ONLY on the provided context.
                                    CONSTRAINT: If info is missing, write "Not Disclosed". Use professional, cautious language.
                                   \s
                                    JUDGMENT CALLS:
                                        - MANAGEMENT TONE: Identify shifts in tone (e.g., concern over raw material prices vs. optimism in rural recovery).
                                        - GROWTH INITIATIVES: Look specifically for 'Beardo', 'Saffola', or 'Digital-first brands'.
                                        - GUIDANCE: Look for 'margin expansion', 'volume growth', or 'revenue targets'.
                                        - HALLUCINATION GUARD: If capacity utilization or a specific metric isn't mentioned, explicitly state 'Metric Not Disclosed'.
                                   \s
                                    STRUCTURE:
                                    1. SENTIMENT: [Optimistic/Cautious/Neutral/Pessimistic] + Evidence.
                                    2. CONFIDENCE: [High/Medium/Low] based on the specificity of guidance.
                                    3. KEY POSITIVES: 3-5 concise bullet points.
                                    4. KEY CONCERNS: 3-5 concise bullet points.
                                    5. GUIDANCE: Revenue, margin, and capex outlook.
                                    6. GROWTH INITIATIVES: 2-3 specific new projects/strategies.
                   \s
                                    CONTEXT:
                                    %s
                   \s""".formatted(context);

            String report = chatModel.call(systemPrompt);

            return Map.of(
                    "report", report,
                    "sourceCount", filteredDocs.size(),
                    "analyzedFile", fileName
            );
        } catch (Exception e) {
            log.error("CRITICAL ERROR in analyzeEarnings: ", e); // This prints the full error in your console
            return Map.of("error", e.getMessage(), "type", e.getClass().getSimpleName());
        }

    }
}
