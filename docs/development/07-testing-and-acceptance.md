# Testing and Acceptance Document

## 1. Objective

This document defines the required test scope, acceptance criteria, and execution rules for the local RAG project.

The execution SOP is documented separately in:

- [sops/01-test-execution-sop.md](sops/01-test-execution-sop.md)

## 2. Test Scope

The required test scope includes:

- Backend unit tests
- Backend integration tests
- Frontend unit tests
- End-to-end tests
- Manual functional verification
- Explainability verification
- Local deployment verification

## 3. Backend Test Coverage

Backend tests must cover:

- Authentication success and failure
- Document upload validation
- Document metadata persistence
- Indexing job lifecycle transitions
- Chunk generation rules
- Retrieval result ordering
- Trace persistence
- Citation persistence
- Settings read and update

## 4. Frontend Test Coverage

Frontend tests must cover:

- Login form behavior
- Document upload page behavior
- Indexing status refresh behavior
- Chat session creation
- Streaming answer rendering
- Citation click behavior
- Evidence panel rendering
- Settings page save behavior

## 5. End-to-End Coverage

End-to-end tests must cover:

1. User login
2. Document upload
3. Document indexing
4. Chat query submission
5. Streaming answer completion
6. Citation review
7. Trace detail review

## 6. Explainability Acceptance

The system is accepted only if explainability output is available for each completed answer.

Required explainability acceptance items:

- The answer contains citation markers
- Citation markers map to persisted evidence items
- Evidence items show document name
- Evidence items show chunk index
- Evidence items show retrieval score
- Evidence items show rerank score
- Evidence items show evidence text

## 7. Functional Acceptance Criteria

The delivery is accepted only if all of the following are true:

- The local environment starts successfully
- A user can log in successfully
- A user can upload a document successfully
- A user can start indexing successfully
- Indexing status can be queried successfully
- A user can create a chat session successfully
- A user can submit a query successfully
- The answer streams successfully
- The final answer is persisted successfully
- The trace detail is persisted successfully
- Evidence items can be viewed successfully
- User settings can be updated successfully

## 8. Failure Acceptance Criteria

The delivery is accepted only if the system handles the following failure cases correctly:

- Invalid login credentials
- Invalid file upload request
- Unsupported document input
- Indexing failure
- Retrieval failure
- Answer generation failure
- Database connection failure
- Redis connection failure

## 9. Data Acceptance Criteria

The delivery is accepted only if:

- Uploaded documents are persisted
- Chunks are persisted
- Vectors are persisted
- Chat sessions are persisted
- Chat messages are persisted
- Trace records are persisted
- Citation mappings are persisted

## 10. Performance Baseline

The local acceptance baseline requires:

- Backend health endpoint responds successfully
- A standard text document can complete upload and indexing in a stable local run
- A standard user query returns a completed answer in a stable local run
- The frontend remains responsive during streaming

## 11. Test Deliverables

The test handoff must contain:

- Automated test result summary
- Manual test result checklist
- Failed test list
- Fixed issue list
- Acceptance sign-off record
