# Test Execution SOP

## 1. Purpose

This SOP defines the standard test execution flow for the Novel Character RAG system, including automated tests and a manual end-to-end (E2E) verification procedure with exact commands.

## 2. Preconditions

Before running any tests, ensure:

- [ ] PostgreSQL is running (`docker compose ps` shows `rag-postgres` healthy)
- [ ] Redis is running (`docker compose ps` shows `rag-redis` healthy)
- [ ] Backend compiles without errors (`cd backend && ./mvnw compile -q`)
- [ ] Frontend builds without errors (`cd frontend && npx vite build`)
- [ ] A valid SiliconFlow API Key is available (for E2E tests)

## 3. Test Levels

| Level | Scope | Tools | SiliconFlow API Required |
|-------|-------|-------|--------------------------|
| Backend Unit Tests | Service/utility logic | Maven Surefire | No |
| Backend Integration Tests | Controller + DB + Redis | Maven Failsafe | No |
| Frontend Build Verification | Vue.js compilation | Vite | No |
| **End-to-End Manual Test** | Full RAG pipeline | curl + API | **Yes** |

## 4. Backend Unit Tests

```bash
cd backend

# Run all unit tests
./mvnw test -q

# Expected output:
# Tests run: XX, Failures: 0, Errors: 0, Skipped: 0
```

**Pass criteria**: 0 failures and 0 errors.

## 5. Backend Compilation Check

```bash
cd backend
./mvnw clean compile -q
# No output = success
# Any output = compilation error
```

## 6. Frontend Build Verification

```bash
cd frontend
npm install   # First time only
npx vite build

# Expected output:
# ✓ XX modules transformed.
# dist/index.html         0.XX kB
# dist/assets/index-*.css XX.XX kB
# dist/assets/index-*.js  XXX.XX kB
# ✓ built in XXXms
```

**Pass criteria**: Build completes with 0 errors.

## 7. End-to-End Manual Test Procedure

This procedure tests the full RAG pipeline: Upload → Chunk → Embed → Index → Retrieve → Rerank → Generate → Trace.

### 7.1 Setup

Start all services:

```bash
# Terminal 1: Infrastructure
docker compose up -d

# Terminal 2: Backend (with real API key)
cd backend
export MODEL_PROVIDER_API_KEY="sk-your-real-siliconflow-api-key"
export JWT_SECRET="my-32-char-long-secure-secret-key"
./mvnw spring-boot:run

# Terminal 3: Frontend
cd frontend
npm run dev
```

Wait for backend to print `Started NovelRagApplication`.

### 7.2 Step 1: Login

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo-password"}' | \
  python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")

echo "Token: ${TOKEN:0:20}..."
```

**Pass criteria**: A JWT token is returned (starts with `eyJ`).

### 7.3 Step 2: Upload a Test Document

Prepare a test text file (e.g., `雷雨.txt` — the included Chinese play by Cao Yu, ~170KB).

```bash
UPLOAD_RES=$(curl -s -X POST http://localhost:8080/api/documents \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@雷雨.txt")

DOC_ID=$(echo "$UPLOAD_RES" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
echo "Document ID: $DOC_ID"
```

**Pass criteria**: Response contains `"documentStatus":"UPLOADED"` and a valid UUID document ID.

### 7.4 Step 3: Trigger Indexing

```bash
curl -s -X POST "http://localhost:8080/api/documents/$DOC_ID/index" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

**Pass criteria**: Response contains `"jobStatus":"PROCESSING"` or `"currentStage":"CHUNKING"`.

### 7.5 Step 4: Poll for Indexing Completion

```bash
# Poll every 5 seconds until complete
for i in $(seq 1 30); do
  sleep 5
  STATUS=$(curl -s "http://localhost:8080/api/documents/$DOC_ID/jobs/latest" \
    -H "Authorization: Bearer $TOKEN" | \
    python3 -c "import sys,json; d=json.load(sys.stdin)['data']; print(f'{d[\"jobStatus\"]}|{d.get(\"totalChunks\",\"?\")}')")
  echo "[$i] Status: $STATUS"
  if echo "$STATUS" | grep -q "COMPLETED"; then break; fi
  if echo "$STATUS" | grep -q "FAILED"; then echo "FAILED!"; exit 1; fi
done
```

**Pass criteria**: Status reaches `COMPLETED` with a non-zero `totalChunks` count.

**Expected results for 雷雨.txt**: ~227 chunks, completed in ~15-30 seconds.

### 7.6 Step 5: Create a Chat Session

```bash
SESSION_ID=$(curl -s -X POST http://localhost:8080/api/chat/sessions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"E2E Test Session"}' | \
  python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")

echo "Session ID: $SESSION_ID"
```

**Pass criteria**: A valid UUID session ID is returned.

### 7.7 Step 6: Send a RAG Query (Streaming)

```bash
# Send a query and capture the SSE stream
timeout 90 curl -s -N -X POST \
  "http://localhost:8080/api/chat/sessions/$SESSION_ID/query/stream" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"query":"周朴园和鲁侍萍是什么关系？请结合原文分析。"}' > /tmp/e2e_sse.txt

# Parse the response
python3 -c "
import json
tokens, trace_id = [], None
with open('/tmp/e2e_sse.txt') as f:
    for line in f:
        line = line.strip()
        raw = None
        if line.startswith('data:data: '): raw = line[11:]
        elif line.startswith('data: '): raw = line[6:]
        if raw:
            try:
                d = json.loads(raw)
                if isinstance(d, dict):
                    if d.get('content'): tokens.append(d['content'])
                    if d.get('traceId') and not trace_id: trace_id = d['traceId']
            except: pass
print(f'Trace ID: {trace_id}')
print(f'Response length: {len(\"\".join(tokens))} chars ({len(tokens)} tokens)')
print()
print('Answer:', ''.join(tokens)[:500])
"
```

**Pass criteria**:
- A `traceId` is present in the SSE events
- The answer is non-empty and relevant to the question
- The response mentions 周朴园 and 侍萍's relationship (前妻/夫妻/抛弃等)

### 7.8 Step 7: Verify Trace (Evidence Citations)

```bash
TRACE_ID="<trace-id-from-step-6>"

curl -s "http://localhost:8080/api/chat/traces/$TRACE_ID" \
  -H "Authorization: Bearer $TOKEN" > /tmp/e2e_trace.json

python3 -c "
import json
with open('/tmp/e2e_trace.json') as f:
    d = json.load(f)['data']
evidence = d.get('evidenceItems', [])
print(f'User query: {d[\"userQuery\"]}')
print(f'Evidence items: {len(evidence)}')
for i, e in enumerate(evidence):
    print(f'  [{i+1}] score={e.get(\"rerankScore\",\"?\")} doc={e.get(\"documentName\",\"?\")} section={e.get(\"sectionLabel\",\"?\")}')
"
```

**Pass criteria**:
- At least 3 evidence items returned
- All evidence items reference the uploaded document (e.g., `雷雨.txt`)
- Rerank scores are > 0.5 for the top items
- Evidence text contains relevant passages from the original document

### 7.9 Step 8: Verify Settings API

```bash
# Check model options
curl -s http://localhost:8080/api/settings/models \
  -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
d = json.load(sys.stdin)['data']
for stage in ['embedding', 'reranking', 'chat']:
    opts = d.get(stage, {}).get('options', [])
    print(f'{stage}: {len(opts)} models available')
"

# Check user settings (including API key status)
curl -s http://localhost:8080/api/settings \
  -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
d = json.load(sys.stdin)['data']
print(f'hasApiKey: {d.get(\"hasApiKey\")}')
print(f'embeddingModel: {d.get(\"embeddingModel\")}')
print(f'chatModel: {d.get(\"chatModel\")}')
"
```

**Pass criteria**:
- Embedding: 3 models (Qwen3 series)
- Reranking: 3 models (Qwen3 series)
- Chat: 18 models (Qwen3, DeepSeek, GLM, Llama, etc.)
- `hasApiKey` is `True`

### 7.10 Step 9: Verify Data Persistence

```bash
# Check original file is persisted on disk
find storage/ -name "*.txt" -exec ls -la {} \;

# Check database records via API
echo "=== Documents ==="
curl -s http://localhost:8080/api/documents \
  -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
d = json.load(sys.stdin)['data']
recs = d.get('records', [])
for r in recs:
    print(f'  {r[\"fileName\"]:30s} status={r[\"documentStatus\"]}')
"

echo "=== Chat Sessions ==="
curl -s http://localhost:8080/api/chat/sessions \
  -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
d = json.load(sys.stdin)['data']
recs = d.get('records', d) if isinstance(d, dict) else d
if isinstance(recs, list):
    for r in recs:
        print(f'  {r.get(\"title\",\"?\"):30s} id={r[\"id\"][:8]}...')
"
```

**Pass criteria**:
- Original file exists on disk under `storage/documents/`
- Document record shows `INDEXED` status
- Chat session and messages are retrievable

## 8. Frontend Visual Verification (Browser)

Open http://localhost:5173 and verify:

| # | Page | Check | Expected |
|---|------|-------|----------|
| 1 | Login | Login with demo/demo-password | Redirects to main page |
| 2 | Document Management (文档管理) | Document list loads | Shows uploaded docs with status |
| 3 | Document Management | Click "开始索引" | Pipeline stages animate: ⬜→⏳→✅ |
| 4 | Document Management | Indexing completes | Progress bar fills, shows "✅ N 个分块已索引" |
| 5 | Chat (对话) | Create session, send question | SSE streaming response renders incrementally |
| 6 | Chat | Click trace button | Evidence cards with scores and original text |
| 7 | Settings (设置) | API Key card | Shows 🔑 input, masked key display, ✅已配置 |
| 8 | Settings | Model dropdowns | Populated from SiliconFlow catalog |
| 9 | Settings | Save button | Shows "✅ 设置已保存" toast |
| 10 | All pages | Scrollbar | Content scrolls when it overflows the viewport |

## 9. E2E Test Summary Template

Use this template to record E2E test results:

```
E2E Test Report
===============
Date: YYYY-MM-DD
Tester:
API Key: sk-****XXXX (masked)

| Step | Result | Notes |
|------|--------|-------|
| Login | PASS/FAIL | |
| Upload | PASS/FAIL | File: ___, Size: ___ |
| Index | PASS/FAIL | Chunks: ___, Time: ___s |
| Query | PASS/FAIL | Answer length: ___ chars |
| Trace | PASS/FAIL | Evidence items: ___, Top score: ___ |
| Settings | PASS/FAIL | Models loaded: ___ |
| Persistence | PASS/FAIL | File on disk: YES/NO |
| Frontend UI | PASS/FAIL | |

Overall: PASS / FAIL
```

## 10. Exit Criteria

Testing can be closed only when:

- [ ] Backend compiles without errors
- [ ] Frontend builds without errors
- [ ] All E2E steps pass (Login → Upload → Index → Query → Trace → Settings)
- [ ] Data is correctly persisted (file on disk, DB records)
- [ ] Frontend visual verification passes (all 10 checks)
- [ ] No blocking defects remain open
