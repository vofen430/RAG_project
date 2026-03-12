# Test Execution SOP

## 1. Purpose

This SOP defines the standard test execution flow for the local RAG project.

## 2. Preconditions

Before running tests, confirm:

- PostgreSQL is running locally
- Redis is running locally
- Required environment variables are configured
- Test database is available
- The backend dependencies are installed
- The frontend dependencies are installed

## 3. Execution Order

Run tests in the following order:

1. Backend unit tests
2. Backend integration tests
3. Frontend unit tests
4. End-to-end tests
5. Manual functional verification

## 4. Backend Unit Test SOP

Steps:

1. Open the project root.
2. Go to the `backend` directory.
3. Run the backend unit test command.
4. Save the test result output.
5. Stop immediately if core module tests fail.

## 5. Backend Integration Test SOP

Steps:

1. Confirm PostgreSQL and Redis are available.
2. Confirm database schema is initialized.
3. Run the backend integration test command.
4. Save the test result output.
5. Record failed cases with module name and error message.

## 6. Frontend Unit Test SOP

Steps:

1. Go to the `frontend` directory.
2. Run the frontend unit test command.
3. Save the test result output.
4. Record failed cases with page or component name.

## 7. End-to-End Test SOP

Steps:

1. Start PostgreSQL and Redis.
2. Start the backend.
3. Start the frontend.
4. Run end-to-end tests.
5. Save screenshots or logs only when a failure occurs.
6. Record failed cases with route and action name.

## 8. Manual Functional Verification SOP

Execute the following checklist in order:

1. Login with a valid user account.
2. Upload a document.
3. Start indexing.
4. Wait for indexing completion.
5. Create a chat session.
6. Submit a question.
7. Wait for the answer stream to complete.
8. Open the trace detail.
9. Open at least one citation entry.
10. Update user settings.
11. Submit answer feedback.

## 9. Defect Recording SOP

For each failed test, record:

- Test level
- Module name
- Exact operation
- Input data
- Actual result
- Expected result
- Error log
- Reproducibility

## 10. Exit Criteria

Testing can be closed only when:

- All critical tests pass
- No blocking defect remains open
- Manual functional verification is complete
- Acceptance checklist is signed off
