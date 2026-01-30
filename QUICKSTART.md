# Quick Reference Guide

## 🚀 Start Application

```bash
cd /Users/vakulkumar/.gemini/antigravity/scratch/earnings-call-rag
./start.sh
```

## 🔑 Setup OpenAI API Key

Option 1: Environment variable
```bash
export OPENAI_API_KEY="sk-your-key-here"
```

Option 2: .env file
```bash
cp .env.example .env
# Edit .env and add your key
```

Get API key: https://platform.openai.com/api-keys

## 📝 Common Commands

### Start PostgreSQL only
```bash
docker-compose up -d
```

### Build project
```bash
mvn clean install
```

### Run application
```bash
mvn spring-boot:run
```

### Run tests
```bash
mvn test
```

### Stop PostgreSQL
```bash
docker-compose down
```

## 🌐 API Examples

Base URL: `http://localhost:8080`

### Upload PDF
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@/path/to/earnings-call.pdf" \
  -F "companyName=Reliance Industries"
```

### Check Status
```bash
curl http://localhost:8080/api/documents/{document-id}/status
```

### Ask Question
```bash
curl -X POST http://localhost:8080/api/questions/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What did the CEO say about AI growth?",
    "companyName": "Reliance Industries"
  }'
```

### List All Documents
```bash
curl http://localhost:8080/api/documents
```

## 🐛 Troubleshooting

### PostgreSQL not starting
```bash
docker-compose down -v
docker-compose up -d
```

### Application won't start
1. Check Java version: `java -version` (need 17+)
2. Check Maven: `mvn -version`
3. Check Docker: `docker ps`
4. Check API key: `echo $OPENAI_API_KEY`

### OpenAI API errors
- Verify API key is valid
- Check your OpenAI account has credits
- Review rate limits

### Database connection errors
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check logs
docker logs earnings-call-postgres
```

## 📚 File Locations

- **Application**: `/Users/vakulkumar/.gemini/antigravity/scratch/earnings-call-rag`
- **Main Class**: `src/main/java/com/earningscall/rag/EarningsCallRagApplication.java`
- **Config**: `src/main/resources/application.yml`
- **Database Schema**: `src/main/resources/schema.sql`
- **README**: `README.md`
- **Architecture Docs**: `ARCHITECTURE.md`

## 🎯 Testing Workflow

1. **Start Application**: `./start.sh`
2. **Upload PDF**: Use curl or Postman
3. **Wait for Processing**: Check status endpoint
4. **Ask Questions**: Use question endpoint
5. **View Logs**: Check console output

## 🔗 Useful Links

- Spring AI Docs: https://docs.spring.io/spring-ai/reference/
- OpenAI API: https://platform.openai.com/docs
- pgvector: https://github.com/pgvector/pgvector
- Apache PDFBox: https://pdfbox.apache.org/

## 💾 Database Access

Connect to PostgreSQL:
```bash
docker exec -it earnings-call-postgres psql -U raguser -d earnings_call_db
```

Useful queries:
```sql
-- List all documents
SELECT id, filename, company_name, processing_status, total_chunks 
FROM documents;

-- Count chunks per document
SELECT document_id, COUNT(*) 
FROM document_chunks 
GROUP BY document_id;

-- Check vector embeddings
SELECT id, chunk_index, page_number 
FROM document_chunks 
LIMIT 5;
```

## 📊 Monitor Application

Watch logs:
```bash
mvn spring-boot:run | grep -E "INFO|ERROR|WARN"
```

Check thread pool:
- Look for "RAG-Async-" threads in logs

Monitor PostgreSQL:
```bash
docker stats earnings-call-postgres
```

## 🎓 Next Steps

1. Set workspace: `File > Open Folder > earnings-call-rag`
2. Test with real PDF from company investor relations
3. Experiment with different questions
4. Customize chunk size in `application.yml`
5. Try different OpenAI models (gpt-3.5-turbo for cost savings)

## ⚙️ Configuration Tuning

Edit `src/main/resources/application.yml`:

```yaml
rag:
  chunk:
    size: 800              # Increase for longer chunks
    overlap: 150           # Increase for more context
  retrieval:
    top-k: 5              # Increase for more sources
    similarity-threshold: 0.7  # Decrease for more results
  async:
    max-pool-size: 10     # Increase for more concurrency
```

---

**Need Help?** Check [README.md](file:///Users/vakulkumar/.gemini/antigravity/scratch/earnings-call-rag/README.md) for detailed documentation.
