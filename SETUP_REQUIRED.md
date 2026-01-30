# Setup Instructions for Running the Application

## ⚠️ Prerequisites Needed

Before running the application, you need:

### 1. Docker Desktop
- **Status**: Not currently running or not installed
- **Install**: Download from https://www.docker.com/products/docker-desktop
- **Note**: Docker is required for PostgreSQL + pgvector database

### 2. OpenAI API Key
- **Get your key**: https://platform.openai.com/api-keys
- **Setup**: Create `.env` file with your key

### 3. Java 17+
- Check: `java -version`

### 4. Maven
- Check: `mvn -version`

---

## 🚀 Quick Setup Steps

### Step 1: Install Docker Desktop
1. Download from https://www.docker.com/products/docker-desktop
2. Install and start Docker Desktop
3. Verify: Open Docker Desktop application

### Step 2: Set OpenAI API Key

Create a `.env` file:
```bash
cd /Users/vakulkumar/.gemini/antigravity/scratch/earnings-call-rag
cp .env.example .env
```

Edit `.env` and add your API key:
```
OPENAI_API_KEY=sk-your-actual-api-key-here
```

### Step 3: Start PostgreSQL

Once Docker is running:
```bash
docker compose up -d
```

### Step 4: Start Spring Boot Application

```bash
export OPENAI_API_KEY="your-api-key-here"
mvn spring-boot:run
```

### Step 5: Open Web Interface

Open in browser:
```
file:///Users/vakulkumar/.gemini/antigravity/scratch/earnings-call-rag/web-interface.html
```

---

## 🌐 I've Created a Web Interface!

A beautiful web interface has been created at:
**`web-interface.html`**

Features:
- 📤 Upload PDF documents
- 📋 View all uploaded documents
- 💬 Ask questions and get AI-powered answers
- 📊 See confidence scores and source citations
- 🎨 Modern, responsive design

---

## 🔧 Alternative: Manual Database Setup

If you don't want to use Docker, you can:

1. Install PostgreSQL 16 manually
2. Install pgvector extension
3. Create database: `earnings_call_db`
4. Update `application.yml` with your database credentials

---

## ❓ Next Steps

1. **Install Docker Desktop** (if not installed)
2. **Start Docker Desktop application**
3. **Set your OpenAI API key** in `.env` file
4. **Run the start script**: `./start.sh` (it will do everything automatically)
5. **Open the web interface** in your browser

---

## 📞 Need Help?

**Docker not installed?**
- Mac: https://docs.docker.com/desktop/install/mac-install/
- Check if running: Look for Docker icon in menu bar

**No OpenAI API key?**
- Sign up: https://platform.openai.com/signup
- Get key: https://platform.openai.com/api-keys
- Note: Requires payment method (free tier available)

**Can't run Maven?**
- Install: `brew install maven` (on Mac)
- Or use IDE (IntelliJ IDEA, VS Code with Java extensions)

---

Once Docker is running and API key is set, you can simply run:
```bash
./start.sh
```

This will start everything automatically!
