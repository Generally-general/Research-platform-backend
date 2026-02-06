package com.research.platform.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class VectorStoreConfig {
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        Path path = Paths.get("vectorstore.json");
        File file = path.toFile();

        SimpleVectorStore vectorStore = new SimpleVectorStore(embeddingModel);

        if(file.exists()) {
            vectorStore.load(file);
        }

        return vectorStore;
    }
}
