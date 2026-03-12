package com.rag.service;

import com.rag.entity.DocumentChunkEntity;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Refactored chunking service producing DocumentChunkEntity objects for DB persistence.
 * Preserves the existing paragraph-aware splitting logic.
 */
@Service
public class ChunkingService {

    // Pattern to match act/scene markers in Chinese plays
    private static final Pattern ACT_PATTERN = Pattern.compile("(第[一二三四五六七八九十百千万\\d]+幕)");

    /**
     * Chunk a document text into persistent chunk entities.
     */
    public List<DocumentChunkEntity> chunkDocument(String documentId, String text,
                                                    int chunkSize, int chunkOverlap,
                                                    String embeddingModel) {
        List<DocumentChunkEntity> chunks = new ArrayList<>();
        List<String> sections = splitByStructure(text);

        int chunkIndex = 0;
        int globalOffset = 0;

        for (int secIdx = 0; secIdx < sections.size(); secIdx++) {
            String section = sections.get(secIdx);
            String sectionLabel = "Section " + (secIdx + 1);

            // Detect act label from section content
            Matcher actMatcher = ACT_PATTERN.matcher(section.substring(0, Math.min(50, section.length())));
            if (actMatcher.find()) {
                sectionLabel = actMatcher.group(1);
            }

            // Find the start offset of this section in the original text
            int sectionStartInText = text.indexOf(section, globalOffset);
            if (sectionStartInText < 0) sectionStartInText = globalOffset;

            // Sub-chunk each section with overlapping windows
            List<SubChunk> subChunks = fixedSizeChunk(section, chunkSize, chunkOverlap);

            for (SubChunk subChunk : subChunks) {
                String trimmed = subChunk.text.trim();
                if (trimmed.isEmpty()) continue;

                DocumentChunkEntity entity = new DocumentChunkEntity();
                entity.setId(UUID.randomUUID().toString());
                entity.setDocumentId(documentId);
                entity.setChunkIndex(chunkIndex);
                entity.setSectionLabel(sectionLabel);
                entity.setContentText(trimmed);
                entity.setContentSummary(trimmed.length() > 100 ? trimmed.substring(0, 100) + "..." : trimmed);
                entity.setStartOffset(sectionStartInText + subChunk.startOffset);
                entity.setEndOffset(sectionStartInText + subChunk.endOffset);
                entity.setEntityList(buildEntityList(trimmed));
                entity.setEmbeddingModel(embeddingModel);
                entity.setContentHash(computeHash(trimmed));

                chunks.add(entity);
                chunkIndex++;
            }

            globalOffset = sectionStartInText + section.length();
        }

        return chunks;
    }

    private List<String> splitByStructure(String text) {
        List<String> sections = new ArrayList<>();
        String[] actParts = ACT_PATTERN.split(text);
        Matcher actMatcher = ACT_PATTERN.matcher(text);
        List<String> actLabels = new ArrayList<>();
        while (actMatcher.find()) {
            actLabels.add(actMatcher.group(1));
        }

        if (actLabels.size() > 1) {
            for (int i = 0; i < actParts.length; i++) {
                String part = actParts[i].trim();
                if (part.isEmpty()) continue;
                if (i > 0 && i - 1 < actLabels.size()) {
                    sections.add(actLabels.get(i - 1) + "\n" + part);
                } else {
                    sections.add(part);
                }
            }
        } else {
            sections.add(text);
        }

        return sections;
    }

    private record SubChunk(String text, int startOffset, int endOffset) {}

    private List<SubChunk> fixedSizeChunk(String text, int size, int overlap) {
        List<SubChunk> chunks = new ArrayList<>();
        if (text.length() <= size) {
            chunks.add(new SubChunk(text, 0, text.length()));
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + size, text.length());
            chunks.add(new SubChunk(text.substring(start, end), start, end));
            if (end >= text.length()) break;
            start = end - overlap;
        }

        return chunks;
    }

    private String buildEntityList(String text) {
        String[] knownCharacters = {
            "周朴园", "蘩漪", "周萍", "周冲", "鲁侍萍", "鲁贵", "鲁大海", "四凤", "繁漪"
        };

        List<String> found = new ArrayList<>();
        for (String name : knownCharacters) {
            if (text.contains(name)) {
                found.add("\"" + name + "\"");
            }
        }

        return "[" + String.join(",", found) + "]";
    }

    private String computeHash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "";
        }
    }
}
