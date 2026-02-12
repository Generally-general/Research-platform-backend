package com.research.platform.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RemoteEmbeddingModel implements EmbeddingModel {
    private final RestClient restClient;

    public RemoteEmbeddingModel(String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://router.huggingface.co/hf-inference/models/sentence-transformers/all-MiniLM-L6-v2")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = request.getInstructions().stream()
                .map(text -> new Embedding(embed(text), 0))
                .collect(Collectors.toList());

        return new EmbeddingResponse(embeddings, new EmbeddingResponseMetadata());
    }

    // This is the method the compiler was missing
    @Override
    public float[] embed(Document document) {
        return embed(document.getContent());
    }

    // Overriding the default to use our API logic
    @Override
    public float[] embed(String text) {
        // The /pipeline/feature-extraction path is appended to the base URL
        List<? extends Number> vector = restClient.post()
                .uri("/pipeline/feature-extraction")
                .body(Map.of("inputs", text))
                .retrieve()
                .body(List.class);

        if (vector == null) return new float[0];

        float[] floatVector = new float[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            floatVector[i] = vector.get(i).floatValue();
        }
        return floatVector;
    }
}