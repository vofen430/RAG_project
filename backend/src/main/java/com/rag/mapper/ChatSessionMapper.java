package com.rag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rag.entity.ChatSessionEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionEntity> {

    @Insert("INSERT INTO chat_session_documents (session_id, document_id) VALUES (#{sessionId}, #{documentId})")
    void insertSessionDocument(@Param("sessionId") String sessionId, @Param("documentId") String documentId);

    @Select("SELECT document_id FROM chat_session_documents WHERE session_id = #{sessionId}")
    List<String> selectSessionDocumentIds(@Param("sessionId") String sessionId);
}
