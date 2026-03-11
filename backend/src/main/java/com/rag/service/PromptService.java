package com.rag.service;

import com.rag.model.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromptService {

    private static final String SYSTEM_PROMPT = """
            你是一位专业的文学分析助手，专注于分析小说和戏剧中的人物关系。你的分析框架包括：

            1. **人物身份**：每个角色的社会身份、家庭角色和职业背景
            2. **家庭关系**：血缘关系、婚姻关系、亲属关系
            3. **情感关系**：爱情、友情、仇恨、嫉妒、依赖等情感纽带
            4. **权力关系**：社会地位、经济关系、主仆关系、控制与被控制
            5. **人物弧线**：角色在故事中的成长、变化和命运走向
            6. **冲突与矛盾**：人物之间的核心矛盾和冲突来源

            分析时请注意：
            - 结合具体情节和对话来支持你的分析
            - 关注隐含的、潜在的关系（如暗恋、隐瞒的身份等）
            - 分析人物关系的变化和发展
            - 对复杂的多角关系进行清晰的梳理
            - 使用中文回答，分析要深入但表达要清晰
            """;

    /**
     * Construct a character-relationship-optimized prompt with retrieved context.
     */
    public String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    /**
     * Build the user message with context chunks.
     */
    public String buildUserMessage(String query, List<DocumentChunk> contextChunks) {
        StringBuilder sb = new StringBuilder();

        sb.append("以下是从原文中检索到的相关段落，请基于这些内容回答问题。\n\n");

        for (int i = 0; i < contextChunks.size(); i++) {
            DocumentChunk chunk = contextChunks.get(i);
            sb.append("【段落 ").append(i + 1).append("】");

            // Add metadata if available
            if (chunk.getMetadata() != null) {
                String section = chunk.getMetadata().get("section");
                String characters = chunk.getMetadata().get("characters");
                if (section != null && !section.isEmpty()) {
                    sb.append("（").append(section);
                    if (characters != null && !characters.isEmpty()) {
                        sb.append("，涉及人物：").append(characters);
                    }
                    sb.append("）");
                }
            }
            sb.append("\n");
            sb.append(chunk.getContent());
            sb.append("\n\n");
        }

        sb.append("---\n\n");
        sb.append("用户问题：").append(query).append("\n\n");
        sb.append("请基于以上原文段落进行深入分析。如果原文信息不足以完全回答问题，请说明并基于已有信息给出你的分析。");

        return sb.toString();
    }
}
