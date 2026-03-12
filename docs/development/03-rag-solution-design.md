# RAG Solution Design Document

## 1. Objective

The RAG solution must support:

- Document upload
- Local indexing
- Retrieval-enhanced answer generation
- Citation-based explainability
- Persistent query trace review

## 2. Supported Document Scope

The first delivery scope supports text-based document ingestion with local file storage and persistent indexing records.

## 3. Pipeline Definition

The RAG pipeline contains the following stages:

1. File ingestion
2. Text extraction
3. Text normalization
4. Chunk generation
5. Metadata extraction
6. Embedding generation
7. Vector persistence
8. Similarity retrieval
9. Reranking
10. Prompt assembly
11. Streaming answer generation
12. Citation persistence
13. Trace persistence

## 4. Ingestion Design

### 4.1 File Ingestion

The backend receives the uploaded file and performs:

- File size validation
- File type validation
- Encoding validation
- Ownership binding
- Local path assignment

### 4.2 Text Extraction

The backend extracts plain text and stores:

- Raw text
- Normalized text
- Source file path
- File hash

## 5. Chunking Design

The chunking stage must produce stable, persistent chunks.

Chunk generation rules:

- Use paragraph-aware splitting
- Preserve chapter or section boundaries when available
- Apply overlap between adjacent chunks
- Assign a stable chunk index
- Persist chunk start offset and end offset
- Persist a short chunk summary for review use

## 6. Metadata Extraction

Each chunk must include metadata fields required by retrieval and explainability.

Required metadata:

- Document ID
- Chunk index
- Section label
- Start offset
- End offset
- Entity list
- Language code
- Hash value

## 7. Embedding Design

Embedding generation rules:

- Generate one embedding per normalized chunk
- Store one vector per chunk
- Keep the embedding dimension fixed for the active model baseline
- Persist the embedding model identifier used during indexing

## 8. Retrieval Design

The retrieval stage is implemented with pgvector in PostgreSQL.

Retrieval rules:

- Generate one query embedding for the incoming user query
- Execute top-k similarity search within the user-authorized document scope
- Filter out inactive or failed document records
- Return chunk text and chunk metadata together
- Persist raw retrieval rank and similarity score

## 9. Reranking Design

The reranking stage receives retrieved chunks and returns a smaller ordered evidence set.

Reranking rules:

- Use the user query and retrieved chunk texts
- Return a ranked evidence list
- Persist rerank score and rerank position
- Keep the final evidence set size fixed by configuration

## 10. Prompt Assembly Design

The prompt assembly stage must build a controlled answer context.

Prompt rules:

- Include the user query
- Include the final evidence set only
- Include citation numbers for each evidence item
- Include section and source location metadata
- Require the answer to stay within cited evidence
- Require the answer to state when evidence is insufficient

## 11. Answer Generation Design

The answer generation stage must stream the final answer to the frontend and persist the completed output.

Output rules:

- Stream token content to the frontend
- Persist the final answer content
- Persist the answer completion timestamp
- Persist the answer status

## 12. Explainability Design

Explainability is a required capability, not an optional add-on.

Required explainability outputs:

- Citation markers in the answer
- Evidence list for the answer
- Evidence text for each citation
- Document name for each citation
- Section label for each citation
- Chunk index for each citation
- Similarity score for each citation
- Rerank score for each citation

## 13. Trace Persistence Design

Each query must generate a trace record.

The trace record must contain:

- Query text
- Query rewrite text when applicable
- Retrieval candidate set
- Retrieval scores
- Final reranked evidence set
- Prompt version
- Answer record ID
- Total latency
- Error details when the request fails

## 14. Chat Session Design

The chat layer must persist:

- Chat session metadata
- User messages
- Assistant messages
- Message order
- Linked trace ID for each assistant answer

## 15. Failure Handling

The pipeline must persist failure state for:

- Upload failure
- Extraction failure
- Chunking failure
- Embedding failure
- Retrieval failure
- Reranking failure
- Answer generation failure

## 16. Local Operation Constraints

This solution is designed for local deployment only.

Local operation requirements:

- All services run on one local machine
- All source files remain on local disk
- PostgreSQL and Redis run locally
- Application logs remain on local disk
