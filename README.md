# Research Insights Platform

A high-performance RAG (Retrieval-Augmented Generation) system designed to extract, index, and query insights from unstructured documents (PDFs, scans, and images). Built with **Java Spring Boot** and **Spring AI**, this project serves as a local proof-of-concept for a system designed to scale to **54 million pages**.

---

## Overview
The platform addresses the challenge of "dark data" in unstructured documents. By combining hybrid OCR extraction with a vector-based retrieval loop, it enables users to ask complex questions and receive answers grounded in specific source text (Citations).

![System Demo](chat.gif)

### Key Features
* **Asynchronous Ingestion:** Uses Spring's event-driven architecture to process heavy files in the background without blocking the UI.
* **Hybrid Extraction:** Automatically switches between **Apache PDFBox** (for text-based PDFs) and **Tesseract OCR** (for scans/images).
* **Citation-First RAG:** Mitigates AI hallucinations by returning raw document chunks alongside LLM-generated answers.
* **Cost-Optimized Infrastructure:** Achieves ₹0 infrastructure cost by using local **ONNX embedding models** and the **Groq Free Tier**.

---

## Tech Stack
* **Backend:** Java 22, Spring Boot 3.3.5
* **AI Orchestration:** Spring AI (Milestone 4)
* **LLM:** Llama 3.3-70b (via Groq Cloud API)
* **Embeddings:** Local Transformers (ONNX models)
* **Vector Store:** SimpleVectorStore (Local JSON persistence)
* **Extraction:** Apache PDFBox, Tess4J (Tesseract)
* **Tools:** Lombok, Slf4j, Maven, Postman

---

## System Architecture



1.  **Ingestion:** User uploads a document via REST API.
2.  **Event Trigger:** An internal `ApplicationEvent` is published, handing off the file to the `DocumentProcessor`.
3.  **Parsing:** The system detects file type and extracts raw text.
4.  **Embedding:** Text is chunked and converted into vectors using a local CPU-bound model.
5.  **Persistence:** Vectors are stored in `vectorstore.json`.
6.  **Chat:** The `/chat` endpoint performs a similarity search, builds a context-rich prompt, and queries the LLM.

---

## Setup & Installation

### Prerequisites
* JDK 22+
* Maven 3.x
* **Tesseract OCR:** [Download here](https://github.com/UB-Mannheim/tesseract/wiki). Ensure `eng.traineddata` is placed in `src/main/resources/tessdata`.

# Run the Application

1. Clone the repository.
2. Run `mvn clean install`.
3. Start the application via your IDE or `mvn spring-boot:run`.

## API Reference

### 1. Upload Document
**Endpoint:** `POST /api/docs/upload`  
**Body:** `form-data` (file)

### 2. Research Chat
**Endpoint:** `GET /api/research/chat?query=What is the main stack?`  
**Response:**
```json
{
  "answer": "The tech stack includes Java, Spring Boot...",
  "citations": ["Raw text chunk 1...", "Raw text chunk 2..."]
}
```

## Challenges Faced & Lessons Learned

* **Model Decommissioning:** Successfully pivoted from `llama3-8b` to `llama-3.3-70b-versatile` on Groq when the initial model was retired mid-development.
* **Local Embedding Optimization:** Solved the "Ollama high-resource" constraint by implementing Local Transformers for embeddings, keeping the local footprint lightweight while offloading reasoning to the cloud.
* **Asynchronous Design:** Implemented non-blocking background tasks using `@Async` to ensure the platform remains responsive even during heavy OCR processing.

## Roadmap

* [ ] Implement Reranker (Cross-Encoders) to improve retrieval precision.
* [ ] Add support for multi-document context merging.
* [ ] Migrate `SimpleVectorStore` to pgvector for production-grade scaling.

---

**Developed by Pranjal Kumar**
