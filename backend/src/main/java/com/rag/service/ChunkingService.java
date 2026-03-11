package com.rag.service;

import com.rag.model.DocumentChunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChunkingService {

    @Value("${rag.chunk-size}")
    private int chunkSize;

    @Value("${rag.chunk-overlap}")
    private int chunkOverlap;

    // Pattern to match act/scene markers in Chinese plays
    private static final Pattern ACT_PATTERN = Pattern.compile("(第[一二三四五六七八九十百千万\\d]+幕)");
    private static final Pattern SCENE_PATTERN = Pattern.compile("(第[一二三四五六七八九十百千万\\d]+场)");

    /**
     * Novel-aware chunking optimized for Chinese literary works.
     * Strategy: split by acts first, then by fixed-size overlapping chunks.
     */
    public List<DocumentChunk> chunkDocument(String documentId, String text) {
        List<DocumentChunk> chunks = new ArrayList<>();

        // First, split by acts/scenes
        List<String> sections = splitByStructure(text);

        int chunkIndex = 0;
        for (int secIdx = 0; secIdx < sections.size(); secIdx++) {
            String section = sections.get(secIdx);
            String sectionLabel = "Section " + (secIdx + 1);

            // Detect act label from section content
            Matcher actMatcher = ACT_PATTERN.matcher(section.substring(0, Math.min(50, section.length())));
            if (actMatcher.find()) {
                sectionLabel = actMatcher.group(1);
            }

            // Sub-chunk each section with overlapping windows
            List<String> subChunks = fixedSizeChunk(section, chunkSize, chunkOverlap);

            for (String subChunk : subChunks) {
                String trimmed = subChunk.trim();
                if (trimmed.isEmpty()) continue;

                Map<String, String> metadata = new LinkedHashMap<>();
                metadata.put("section", sectionLabel);
                metadata.put("characters", extractCharacterNames(trimmed));

                String chunkId = documentId + "_chunk_" + chunkIndex;
                chunks.add(new DocumentChunk(chunkId, documentId, trimmed, chunkIndex, metadata));
                chunkIndex++;
            }
        }

        return chunks;
    }

    /**
     * Split text by act/scene markers; if none found, treat whole text as one section.
     */
    private List<String> splitByStructure(String text) {
        List<String> sections = new ArrayList<>();

        // Try splitting by acts
        String[] actParts = ACT_PATTERN.split(text);
        Matcher actMatcher = ACT_PATTERN.matcher(text);
        List<String> actLabels = new ArrayList<>();
        while (actMatcher.find()) {
            actLabels.add(actMatcher.group(1));
        }

        if (actLabels.size() > 1) {
            // Re-assemble with act labels prepended
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
            // No act markers, just use the whole text
            sections.add(text);
        }

        return sections;
    }

    /**
     * Fixed-size chunking with overlap (in characters, not tokens).
     */
    private List<String> fixedSizeChunk(String text, int size, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text.length() <= size) {
            chunks.add(text);
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + size, text.length());
            chunks.add(text.substring(start, end));
            if (end >= text.length()) break;
            start = end - overlap;
        }

        return chunks;
    }

    /**
     * Extract character names mentioned in the chunk.
     * For 《雷雨》, common character names are detected.
     */
    private String extractCharacterNames(String text) {
        // Common character names in 雷雨 and other Chinese novels
        String[] knownCharacters = {
            "周朴园", "蘩漪", "周萍", "周冲", "鲁侍萍", "鲁贵", "鲁大海", "四凤",
            "繁漪",  // alternate writing
            // Generic patterns that might be character names (single-surname dialogue markers)
        };

        List<String> found = new ArrayList<>();
        for (String name : knownCharacters) {
            if (text.contains(name)) {
                found.add(name);
            }
        }

        return String.join(", ", found);
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void setChunkOverlap(int chunkOverlap) {
        this.chunkOverlap = chunkOverlap;
    }

    public int getChunkSize() { return chunkSize; }
    public int getChunkOverlap() { return chunkOverlap; }
}
