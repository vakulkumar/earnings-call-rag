#!/bin/bash

echo "🚀 Starting Earnings Call RAG Application"
echo "=========================================="
echo ""

# Check if .env file exists
if [ ! -f .env ]; then
    echo "⚠️  .env file not found. Creating from .env.example..."
    cp .env.example .env
    echo "📝 Please edit .env and add your OPENAI_API_KEY"
    echo ""
    read -p "Press Enter after adding your API key to .env, or Ctrl+C to exit..."
fi

# Load environment variables
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

# Check if OPENAI_API_KEY is set
if [ -z "$OPENAI_API_KEY" ] || [ "$OPENAI_API_KEY" = "your-openai-api-key-here" ]; then
    echo "❌ OPENAI_API_KEY not configured in .env"
    echo "Please add your OpenAI API key to the .env file"
    exit 1
fi

echo "✅ OpenAI API key found"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Start PostgreSQL
echo "🐘 Starting PostgreSQL with pgvector..."
docker-compose up -d

# Wait for PostgreSQL to be ready
echo "⏳ Waiting for PostgreSQL to be ready..."
sleep 5

until docker exec earnings-call-postgres pg_isready -U raguser -d earnings_call_db > /dev/null 2>&1; do
    echo "   Still waiting..."
    sleep 2
done

echo "✅ PostgreSQL is ready"
echo ""

# Build and run the application
echo "🔨 Building application..."
mvn clean install -DskipTests

echo ""
echo "🚀 Starting Spring Boot application..."
echo "📍 Application will be available at: http://localhost:8080"
echo "📖 API Documentation in README.md"
echo ""
echo "Press Ctrl+C to stop the application"
echo "=========================================="
echo ""

mvn spring-boot:run
