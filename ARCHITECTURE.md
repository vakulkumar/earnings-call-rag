# Architecture Documentation

## System Overview

The Earnings Call RAG Application is a production-ready system that combines modern AI capabilities with enterprise Java patterns. It enables users to upload PDF documents and query them using natural language.

## Core Technologies

### Spring AI Framework

Spring AI is the official Spring framework for AI integration, providing:
- Unified abstraction over different LLM providers
- Built-in vector store integrations
- Streaming support
- Retry and error handling

**Why Spring AI?**
- First-class Spring Boot integration
- Production-ready patterns
- Strong typing and compile-time safety
- Active development by Spring team

### PostgreSQL with pgvector

pgvector is a PostgreSQL extension for vector similarity search:
- Native vector data type
- Multiple distance functions (cosine, L2, inner product)
- HNSW and IVFFlat indexing algorithms
- Excellent performance for < 1M vectors

**Performance Characteristics:**
- HNSW index: O(log n) search complexity
- Cosine similarity for semantic search
- Efficient nearest neighbor retrieval

## Architectural Patterns

### 1. Async Processing with CompletableFuture

PDF processing is inherently slow and should not block API responses:

```java
@Async("taskExecutor")
public CompletableFuture<Void> processDocumentAsync(UUID documentId, MultipartFile file) {
    return CompletableFuture.runAsync(() -> {
        // Extract, chunk, embed, store
    });
}
```

**Benefits:**
- Non-blocking API responses
- Better resource utilization
- Scalable under load
- User gets immediate feedback

### 2. RAG Pipeline

**Three-Stage Process:**

#### Stage 1: Retrieval
```
User Question → Embedding (1536-dim vector) → 
  pgvector Similarity Search → Top-K Chunks
```

#### Stage 2: Augmentation
```
Retrieved Chunks → Context Assembly → 
  Prompt Template → Enhanced Prompt
```

#### Stage 3: Generation
```
Enhanced Prompt → OpenAI GPT-4 → 
  Contextual Answer + Citations
```

### 3. Document Chunking Strategy

**Sliding Window Approach:**
- Chunk size: 800 characters
- Overlap: 150 characters
- Sentence boundary preservation

**Why Overlap?**
- Prevents loss of context at boundaries
- Improves retrieval quality
- Better for questions spanning multiple chunks

**Visualization:**
```
Document: [AAAAAABBBBBBCCCCCCDDDDDD]
Chunks:   [AAAAAA]
              [BBBBBBB]
                  [CCCCCC]
                      [DDDDDD]
         └─overlap─┘
```

### 4. Embedding Strategy

**OpenAI text-embedding-ada-002:**
- 1536 dimensions
- Optimized for semantic search
- ~$0.10 per 1M tokens
- Batch processing for efficiency

**Embedding Pipeline:**
```
Text Chunk → Tokenization → 
  Embedding Model → 1536-dim Vector → 
  pgvector Storage
```

## Data Flow

### Upload Flow

```
1. Client uploads PDF
   ↓
2. DocumentController validates file
   ↓
3. Create Document entity (status: PENDING)
   ↓
4. Return documentId immediately
   ↓
5. Async: Extract text (PDFBox)
   ↓
6. Async: Chunk text (TextChunker)
   ↓
7. Async: Generate embeddings (OpenAI)
   ↓
8. Async: Store in pgvector
   ↓
9. Update status to COMPLETED
```

### Query Flow

```
1. Client asks question
   ↓
2. Generate question embedding
   ↓
3. Similarity search in pgvector
   ↓
4. Retrieve top-5 most similar chunks
   ↓
5. Build context from chunks
   ↓
6. Create prompt with context + question
   ↓
7. Send to GPT-4
   ↓
8. Return answer + citations
```

## Database Schema

### documents table
```sql
- id (UUID, PK)
- filename (VARCHAR)
- company_name (VARCHAR)
- upload_timestamp (TIMESTAMP)
- processing_status (ENUM: PENDING, PROCESSING, COMPLETED, FAILED)
- total_chunks (INTEGER)
- metadata (JSONB)
```

### document_chunks table
```sql
- id (UUID, PK)
- document_id (UUID, FK → documents)
- chunk_text (TEXT)
- chunk_index (INTEGER)
- page_number (INTEGER)
- metadata (JSONB)
```

### Vector Store (Spring AI managed)
```sql
- embedding (vector(1536))
- content (TEXT)
- metadata (JSONB)
```

## Scalability Considerations

### Current Design
- Single instance
- Shared PostgreSQL
- Synchronous LLM calls

### Production Scaling

**Horizontal Scaling:**
- Multiple app instances behind load balancer
- Shared PostgreSQL cluster
- Redis for distributed caching

**Database Scaling:**
- PostgreSQL read replicas
- Connection pooling (HikariCP)
- pgvector index optimization

**LLM Optimization:**
- Response caching
- Batch embedding generation
- Rate limiting and quotas

**Storage:**
- Store PDFs in S3/object storage
- Stream large files
- CDN for static content

## Performance Metrics

**Target Performance:**
- PDF Upload Response: < 200ms (async)
- Processing Time: ~1-2 seconds per page
- Query Response: < 3 seconds
- Embedding Generation: ~500ms per batch (10 chunks)
- Vector Search: < 100ms

**Bottlenecks:**
1. OpenAI API latency (800-2000ms)
2. PDF text extraction (CPU-bound)
3. Embedding generation (network-bound)

**Optimizations:**
- Async processing eliminates upload blocking
- Batch embedding reduces API calls
- HNSW index accelerates vector search
- Connection pooling reduces overhead

## Security Considerations

**Current Implementation:**
- API key in environment variables
- No authentication/authorization
- Public endpoints

**Production Requirements:**
- JWT/OAuth2 authentication
- Rate limiting per user
- Input validation and sanitization
- SQL injection prevention (JPA)
- File upload size limits
- Virus scanning for uploads

## Monitoring & Observability

**Recommended Additions:**
1. **Metrics:** Micrometer + Prometheus
2. **Logging:** Structured logging with correlation IDs
3. **Tracing:** Spring Cloud Sleuth + Zipkin
4. **Alerting:** Failed processing jobs, high latency

## Error Handling

**Strategy:**
- Optimistic processing with status tracking
- Failed documents marked as FAILED
- Detailed error logging
- User-friendly error messages

**Recovery:**
- Manual retry via re-upload
- Automatic retry for transient failures (future)
- Dead letter queue for failed embeddings (future)

## Cost Analysis

**OpenAI Costs (per document):**
- Embedding: ~$0.001 per 100 pages
- GPT-4 Query: ~$0.03 per query
- Monthly estimate (100 docs, 1000 queries): ~$30-50

**Infrastructure:**
- PostgreSQL: Free (Docker) or $15-30/month (managed)
- Application hosting: $10-50/month (VPS/cloud)

## Future Enhancements

1. **Multi-modal Support:** Process images, tables, charts
2. **Streaming Responses:** SSE for real-time answers
3. **Caching Layer:** Redis for repeated queries
4. **Advanced Chunking:** Semantic chunking using NLP
5. **Fine-tuning:** Company-specific model fine-tuning
6. **Analytics Dashboard:** Usage metrics, popular queries
7. **Collaborative Features:** Shared documents, annotations

## Conclusion

This architecture demonstrates modern AI engineering practices in Java:
- ✅ Proper async patterns
- ✅ Vector database integration
- ✅ Production-ready RAG pipeline
- ✅ Scalable design
- ✅ Clear separation of concerns

Perfect for portfolios and real-world applications!
