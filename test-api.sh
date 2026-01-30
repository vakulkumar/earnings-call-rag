#!/bin/bash

# API Testing Script for Earnings Call RAG Application

BASE_URL="http://localhost:8080"

echo "🧪 Earnings Call RAG - API Testing Suite"
echo "========================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test 1: Health Check
echo -e "${BLUE}Test 1: Health Check${NC}"
curl -s ${BASE_URL}/api/questions/health
echo -e "\n"

# Test 2: Upload Document
echo -e "${BLUE}Test 2: Upload Document${NC}"
echo "Note: You need to have a PDF file to test this"
echo "Example command:"
echo "  curl -X POST ${BASE_URL}/api/documents/upload \\"
echo "    -F \"file=@your-earnings-call.pdf\" \\"
echo "    -F \"companyName=Your Company\""
echo -e "\n"

# Test 3: List Documents
echo -e "${BLUE}Test 3: List All Documents${NC}"
curl -s ${BASE_URL}/api/documents | jq '.'
echo -e "\n"

# Test 4: Ask Question (you'll need to update document ID)
echo -e "${BLUE}Test 4: Ask Question${NC}"
echo "Example command:"
echo "  curl -X POST ${BASE_URL}/api/questions/ask \\"
echo "    -H \"Content-Type: application/json\" \\"
echo "    -d '{\"question\": \"What did the CEO say about growth?\"}' | jq '.'"
echo -e "\n"

echo "========================================"
echo "✅ Basic tests completed!"
echo ""
echo "To test the full workflow:"
echo "1. Upload a PDF using the upload endpoint"
echo "2. Check its status until COMPLETED"
echo "3. Ask questions about the document"
echo ""
