package com.rag.service;

import com.rag.entity.DocumentChunkEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Prompt assembly service producing citation-numbered prompts.
 */
@Service
public class PromptService {

    public static final String PROMPT_VERSION = "v1.0";

    private static final String SYSTEM_PROMPT = """
            You are a professional document analysis assistant. Your task is to answer user questions
            based ONLY on the provided evidence passages. Follow these rules strictly:

            1. Use citation markers [1], [2], etc. to reference evidence in your answer.
            2. Every factual claim must be supported by at least one citation.
            3. If the evidence is insufficient to fully answer the question, state that clearly.
            4. Do not fabricate information not present in the evidence.
            5. Structure your answer clearly with proper formatting.
            6. Answer in the same language as the user's question.
            """;

    public String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    /**
     * Build the user message with citation-numbered evidence chunks.
     */
    public String buildUserMessage(String query, List<RerankService.RerankResult> evidenceItems) {
        StringBuilder sb = new StringBuilder();

        sb.append("Below are evidence passages retrieved from the documents. ");
        sb.append("Use citation markers [N] to reference them in your answer.\n\n");

        for (int i = 0; i < evidenceItems.size(); i++) {
            RerankService.RerankResult item = evidenceItems.get(i);
            DocumentChunkEntity chunk = item.chunk();
            int citationNo = i + 1;

            sb.append("[").append(citationNo).append("] ");
            if (chunk.getSectionLabel() != null && !chunk.getSectionLabel().isEmpty()) {
                sb.append("(").append(chunk.getSectionLabel()).append(") ");
            }
            sb.append("\n");
            sb.append(chunk.getContentText());
            sb.append("\n\n");
        }

        sb.append("---\n\n");
        sb.append("User question: ").append(query).append("\n\n");
        sb.append("Answer the question based on the evidence above. ");
        sb.append("Use citation markers [1], [2], etc. If evidence is insufficient, state so clearly.");

        return sb.toString();
    }
}
