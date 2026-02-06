package com.research.platform.controller;

import lombok.RequiredArgsConstructor;
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
public class ChatController {
    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    @GetMapping("/chat")
    public Map<String, Object> chat(@RequestParam(value = "query") String query) {
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.query(query).withTopK(3)
        );

        String context = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n"));

        String prompt = """
                You are a Research Assistant. Use the following context to answer the question.
                If the answer isn't in the context, say you don't know.
                
                CONTEXT:
                %s
                
                QUESTION:
                %s
                """.formatted(context, query);

        String answer =  chatModel.call(prompt);

        return Map.of(
                "answer", answer,
                "citations", similarDocuments.stream().map(Document::getContent).toList()
        );
    }
}
