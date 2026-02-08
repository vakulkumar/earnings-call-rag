# Earnings Call RAG Application

🚀 A cutting-edge **RAG (Retrieval-Augmented Generation)** application built with **Spring AI** that enables intelligent querying of PDF earnings call transcripts using advanced LLM technology.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M4-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 🎬 Demo

**Premium Web Interface** - A professional-grade chat UI matching industry leaders like Gemini, ChatGPT, and Grok.

Open `web-interface.html` in your browser to experience:
- 🌙 Dark theme with glassmorphism effects
- 💬 Chat-based interface with message bubbles
- 📤 Drag-and-drop PDF uploads
- ✨ Smooth 60fps animations
- 📱 Fully responsive design

## 🎯 Overview

This application demonstrates state-of-the-art AI integration in Java, allowing users to:
- Upload PDF documents of earnings call transcripts (e.g., Reliance, Infosys)
- Ask natural language questions like *"What did the CEO say about AI growth?"*
- Receive accurate, context-aware answers with source citations

### Key Features

✨ **Spring AI Integration** - Leveraging the official Spring AI framework for seamless LLM integration  
🔍 **Vector Search** - PostgreSQL with pgvector extension for semantic similarity search  
⚡ **Async Processing** - Non-blocking PDF processing using `CompletableFuture`  
📊 **Smart Chunking** - Intelligent document chunking with overlap for better context  
🎯 **RAG Pipeline** - Complete implementation of Retrieval-Augmented Generation  
📚 **Source Citations** - Answers include document name, page numbers, and similarity scores  
🎨 **Premium UI** - Professional chat interface rivaling Gemini/ChatGPT quality  
📱 **Responsive Design** - Works beautifully on desktop, tablet, and mobile  

## 🏗️ Architecture

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────┐
│         REST API Layer                   │
│  ┌────────────┐    ┌────────────┐       │
│  │ Document   │    │ Question   │       │
│  │ Controller │    │ Controller │       │
│  └────────────┘    └────────────┘       │
└──────┬─────────────────┬────────────────┘
       │                 │
       ▼                 ▼
┌─────────────────────────────────────────┐
│          Service Layer                   │
│  ┌────────────────┐  ┌────────────┐     │
│  │ PDF Processing │  │ RAG Service│     │
│  │   (Async)      │  │            │     │
│  └────────────────┘  └────────────┘     │
│  ┌────────────────┐  ┌────────────┐     │
│  │ Embedding      │  │ Vector     │     │
│  │ Service        │  │ Storage    │     │
│  └────────────────┘  └────────────┘     │
└──────┬─────────────────┬────────────────┘
       │                 │
       ▼                 ▼
┌──────────────┐   ┌──────────────┐
│ PostgreSQL   │   │  OpenAI API  │
│  + pgvector  │   │   (GPT-4)    │
└──────────────┘   └──────────────┘
```

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 3.2, Spring AI 1.0.0-M4 |
| Language | Java 17 |
| Database | PostgreSQL 16 with pgvector |
| LLM | OpenAI GPT-4 + text-embedding-ada-002 |
| PDF Processing | Apache PDFBox 3.0 |
| Build Tool | Maven |
| Async | Java CompletableFuture, Spring @Async |

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **Docker** and **Docker Compose** (for PostgreSQL)
- **OpenAI API Key** (Get one from [OpenAI Platform](https://platform.openai.com/api-keys))

## 🚀 Quick Start

### 1. Clone and Navigate

```bash
cd earnings-call-rag
```

### 2. Set Up Environment

Create a `.env` file with your OpenAI API key:

```bash
cp .env.example .env
# Edit .env and add your OpenAI API key
```

Or export it directly:

```bash
export OPENAI_API_KEY="your-api-key-here"
```

### 3. Start PostgreSQL with pgvector

```bash
docker-compose up -d
```

This starts PostgreSQL 16 with pgvector extension on port 5432.

### 4. Build the Application

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📖 API Documentation

### Upload PDF Document

**Endpoint:** `POST /api/documents/upload`

**Request:**
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@reliance-earnings-q4-2024.pdf" \
  -F "companyName=Reliance Industries"
```

**Response:**
```json
{
  "documentId": "123e4567-e89b-12d3-a456-426614174000",
  "filename": "reliance-earnings-q4-2024.pdf",
  "companyName": "Reliance Industries",
  "status": "PENDING",
  "uploadTimestamp": "2024-01-30T10:30:00",
  "message": "Document uploaded successfully. Processing has started."
}
```

### Check Document Status

**Endpoint:** `GET /api/documents/{id}/status`

**Request:**
```bash
curl http://localhost:8080/api/documents/123e4567-e89b-12d3-a456-426614174000/status
```

**Response:**
```json
{
  "documentId": "123e4567-e89b-12d3-a456-426614174000",
  "filename": "reliance-earnings-q4-2024.pdf",
  "status": "COMPLETED",
  "message": "Document processed successfully. 45 chunks created."
}
```

### Ask a Question

**Endpoint:** `POST /api/questions/ask`

**Request:**
```bash
curl -X POST http://localhost:8080/api/questions/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What did the CEO say about AI growth?",
    "companyName": "Reliance Industries"
  }'
```

**Response:**
```json
{
  "answer": "According to the Q4 2024 earnings call, the CEO mentioned that AI initiatives are a key focus area, with investments planned in AI-driven retail and telecommunications. The company expects AI to drive 15-20% efficiency gains in operations over the next 2 years.",
  "confidenceScore": 0.89,
  "sources": [
    {
      "documentName": "reliance-earnings-q4-2024.pdf",
      "pageNumber": 12,
      "relevantText": "Our AI initiatives are transforming how we approach retail and telecommunications...",
      "similarityScore": 0.92
    },
    {
      "documentName": "reliance-earnings-q4-2024.pdf",
      "pageNumber": 15,
      "relevantText": "We anticipate AI to drive significant efficiency gains, approximately 15-20% over...",
      "similarityScore": 0.87
    }
  ],
  "processingTimeMs": 2341
}
```

### List All Documents

**Endpoint:** `GET /api/documents`

**Request:**
```bash
curl http://localhost:8080/api/documents
```

## 🧪 Testing the Application

### 1. Create a Sample PDF

You can create a sample earnings call transcript or use a real one from company investor relations pages.

### 2. Upload the PDF

```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@earnings-call.pdf" \
  -F "companyName=Test Company"
```

### 3. Wait for Processing

Check status until it shows `COMPLETED`:

```bash
curl http://localhost:8080/api/documents/{document-id}/status
```

### 4. Ask Questions

```bash
curl -X POST http://localhost:8080/api/questions/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What were the revenue numbers?"}'
```

## 🔧 Configuration

### Application Properties

Key configurations in `application.yml`:

```yaml
rag:
  chunk:
    size: 800              # Characters per chunk
    overlap: 150           # Overlap between chunks
  retrieval:
    top-k: 5              # Number of chunks to retrieve
    similarity-threshold: 0.7  # Minimum similarity score
  async:
    core-pool-size: 5     # Thread pool size
    max-pool-size: 10
```

### Database Configuration

PostgreSQL with pgvector runs on:
- Host: `localhost`
- Port: `5432`
- Database: `earnings_call_db`
- User: `raguser`
- Password: `ragpassword`

## 🎓 How It Works

### 1. PDF Upload & Processing (Async)

```java
@Async("taskExecutor")
public CompletableFuture<Void> processDocumentAsync(UUID documentId, MultipartFile file) {
    // Extract text from PDF using PDFBox
    // Chunk text with sliding window (800 chars, 150 overlap)
    // Generate embeddings using OpenAI
    // Store in pgvector
}
```

### 2. Semantic Search

When a question is asked:
1. Convert question to embedding (1536 dimensions)
2. Perform cosine similarity search in pgvector
3. Retrieve top-K most relevant chunks

### 3. RAG Pipeline

```
Question → Embedding → Vector Search → Top Chunks → 
  Context Building → LLM Prompt → Answer Generation
```

## 📊 Performance

- **PDF Processing**: Asynchronous, non-blocking
- **Embedding Generation**: Batch processing
- **Vector Search**: HNSW index for O(log n) performance
- **Query Response**: ~2-3 seconds end-to-end

## 🎨 Web Interface

The application includes a **premium web interface** (`web-interface.html`) with:

### Features
- **Dark Theme** - Modern dark color palette with purple accents
- **Chat Interface** - Conversation-style message bubbles
- **Glassmorphism** - Backdrop blur and frosted glass effects
- **Animations** - Smooth 60fps transitions and loading states
- **Drag & Drop** - Upload PDFs by dragging into the browser
- **Responsive** - Works on desktop, tablet, and mobile
- **Source Citations** - View document sources for each answer

### Using the Web Interface

1. Start the backend server:
```bash
./start.sh
```

2. Open `web-interface.html` in your browser

3. Upload a PDF using the upload button or drag & drop

4. Ask questions in the chat input

## 📁 Project Structure

```
earnings-call-rag/
├── src/main/java/com/earningscall/rag/
│   ├── EarningsCallRagApplication.java
│   ├── config/
│   │   └── AsyncConfig.java
│   ├── controller/
│   │   ├── DocumentController.java
│   │   └── QuestionController.java
│   ├── model/
│   │   ├── Document.java
│   │   ├── DocumentChunk.java
│   │   └── dto/
│   ├── repository/
│   │   ├── DocumentRepository.java
│   │   └── DocumentChunkRepository.java
│   ├── service/
│   │   ├── EmbeddingService.java
│   │   ├── PdfProcessingService.java
│   │   ├── RagService.java
│   │   └── VectorStorageService.java
│   └── util/
│       ├── PdfTextExtractor.java
│       └── TextChunker.java
├── src/main/resources/
│   ├── application.yml
│   └── schema.sql
├── web-interface.html        # Premium chat UI
├── docker-compose.yml        # PostgreSQL + pgvector
├── start.sh                  # Startup script
├── test-api.sh              # API testing script
├── README.md
├── ARCHITECTURE.md          # Technical deep-dive
├── QUICKSTART.md            # Quick reference
└── pom.xml
```

## 🤝 Contributing

This is a portfolio/learning project. Feel free to fork and experiment!

## 📄 License

MIT License - feel free to use this project for learning and portfolio purposes.

## 🙏 Acknowledgments

- Spring AI Team for the amazing framework
- PostgreSQL pgvector for vector similarity search
- Apache PDFBox for PDF processing
- OpenAI for GPT-4 and embeddings

## 📧 Contact

For questions about this project, please open an issue on GitHub.

---

**Built with ❤️ using Spring AI**
