# Research Insights Platform

A document analysis tool built with Java Spring Boot and Spring AI. It extracts text from PDFs (including scans) and uses an LLM (Large Language Model) to answer questions based on the document content.

## Core Logic

* **Hybrid Extraction**: Uses PDFBox for digital text and Tesseract OCR for scanned images.
* **Background Processing**: Files are processed asynchronously using Spring Events so the UI doesn't freeze.
* **RAG (Retrieval-Augmented Generation)**: Chunks text into vectors and stores them locally in `vectorstore.json`.
* **LLM Integration**: Uses Groq (Llama 3.3-70b) to generate answers grounded in the extracted text.

## Tech Stack

* **Language**: Java 22
* **Framework**: Spring Boot 3.3.5
* **AI**: Spring AI (Milestone 4)
* **OCR**: Tesseract (via Tess4J)
* **Embeddings**: Local ONNX models (runs on CPU)
* **API**: Groq Cloud

## How it Works

1. **Upload**: User sends a PDF via the `/api/docs/upload` endpoint.
2. **Process**: The system extracts text. If no text is found, it automatically runs OCR.
3. **Index**: Text is converted into vectors and saved to a local JSON file.
4. **Analyze**: The `/api/research/earning-call-summary` endpoint retrieves the most relevant text chunks and sends them to the LLM to generate a structured report.

## Setup & Environment Variables

To run this project (locally or on Koyeb), you must set:

* `GROQ_API_KEY`: Your API key from Groq Console.
* `TESSDATA_PREFIX`: Path to your Tesseract training data (usually `src/main/resources/tessdata`).

## Local Run

1. Install Tesseract OCR on your machine.
2. Clone the repo and run `mvn spring-boot:run`.
3. The server starts on `port 8080`.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/docs/upload` | Uploads a PDF/Image for indexing. |
| `GET` | `/api/research/earning-call-summary` | Returns a structured analyst report for a specific file. |

## Project Status (MVP)

* [x] Hybrid OCR for scanned PDFs.
* [x] Local vector storage (No database required).
* [x] Structured Analyst Report prompt.
* [x] Frontend integration with loading states.

---

**Developed by Pranjal Kumar**