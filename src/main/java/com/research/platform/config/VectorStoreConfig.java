package com.research.platform.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.huggingface.HuggingfaceChatModel;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class VectorStoreConfig {
    @Value("${spring.ai.huggingface.chat.api-key}")
    private String apiKey;

    @Bean
    public EmbeddingModel embeddingModel() {
        return new RemoteEmbeddingModel(apiKey);
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return new SimpleVectorStore(embeddingModel);
    }
}
