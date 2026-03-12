package com.rag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rag.entity.DocumentChunkEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper for document_chunks table.
 * Custom methods for pgvector operations are defined in the XML mapper.
 */
@Mapper
public interface DocumentChunkMapper extends BaseMapper<DocumentChunkEntity> {

    /**
     * Insert a chunk including its embedding vector (pgvector type).
     */
    void insertWithVector(@Param("entity") DocumentChunkEntity entity,
                          @Param("vector") String vectorString);

    /**
     * Search for similar chunks using cosine distance with pgvector.
     * Returns chunks ordered by similarity (closest first).
     * Filters by document IDs that belong to the user's authorized scope.
     */
    List<DocumentChunkEntity> searchSimilar(@Param("vector") String vectorString,
                                             @Param("topK") int topK,
                                             @Param("userId") String userId);

    /**
     * Search for similar chunks scoped to specific document IDs.
     */
    List<DocumentChunkEntity> searchSimilarByDocIds(@Param("vector") String vectorString,
                                                     @Param("topK") int topK,
                                                     @Param("documentIds") List<String> documentIds);

    /**
     * Delete all chunks for a document.
     */
    void deleteByDocumentId(@Param("documentId") String documentId);
}
