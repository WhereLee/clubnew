package com.club.agent.rag;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import com.club.agent.AbstractAgentTool;
import com.club.agent.AgentContext;
import com.club.agent.AgentToolResult;
import com.club.agent.ToolAccess;
import com.club.mapper.AgentToolLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 知识库检索（RAG 召回层）：语义检索平台规则文档，返回 topK 参考片段。
 * 回答必须基于返回片段，不编造平台规则（prompt 层约束 + 工具描述约束双保险）。
 *
 * 工具权限 ALL：平台规则对所有登录用户公开，不涉及私有数据。
 * embedding 模型未配置时（CI/无 BGE_MODEL_URI）注入 DisabledVectorStore，降级应答。
 */
@Component
public class KnowledgeSearchTool extends AbstractAgentTool {

    private static final int TOP_K = 4;
    private static final double SIMILARITY_THRESHOLD = 0.35;

    private final VectorStore knowledgeVectorStore;

    public KnowledgeSearchTool(AgentToolLogMapper toolLogMapper, ObjectMapper objectMapper,
                               VectorStore knowledgeVectorStore) {
        super(toolLogMapper, objectMapper);
        this.knowledgeVectorStore = knowledgeVectorStore;
    }

    @Override
    public String name() {
        return "search_platform_knowledge";
    }

    @Override
    public String description() {
        return "检索平台规则知识库（入社流程、报名规则、活动签到、经费申请、社团创建、行为规范等）。"
                + "适用场景：用户问「怎么入社」「报名后能取消吗」「签到规则是什么」「经费怎么申请」等规则类问题。"
                + "回答必须基于返回的参考片段，不得编造规则。参数 query：用户问题的关键词或短句。";
    }

    @Override
    public ToolAccess access() {
        return ToolAccess.ALL;
    }

    @Tool(description = "语义检索平台规则知识库，返回相关参考片段")
    public String searchPlatformKnowledge(
            @ToolParam(description = "检索查询（用户问题关键词/短句）") String query,
            ToolContext toolContext) {
        return bridge(toolContext, Map.of("query", query)).content();
    }

    @Override
    protected AgentToolResult doExecute(Map<String, Object> args, AgentContext ctx) {
        if (knowledgeVectorStore instanceof DisabledVectorStore) {
            return AgentToolResult.of("知识库当前未启用：服务器未配置本地 embedding 模型（BGE_MODEL_URI）。"
                    + "请告知用户该能力暂不可用，可咨询管理员。");
        }
        String query = String.valueOf(args.getOrDefault("query", ""));
        if (query.isBlank()) {
            return AgentToolResult.of("检索失败：query 为空。");
        }
        List<Document> hits = knowledgeVectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(TOP_K)
                        .similarityThreshold(SIMILARITY_THRESHOLD)
                        .build());

        if (hits.isEmpty()) {
            return AgentToolResult.of("知识库中没有检索到与「" + query + "」相关的规则内容。"
                    + "请告知用户该问题暂无官方规则，建议咨询社团负责人或管理员。");
        }

        StringBuilder sb = new StringBuilder("【知识库参考片段】（请仅基于以下内容回答，标注规则出处）\n");
        for (int i = 0; i < hits.size(); i++) {
            Document doc = hits.get(i);
            String title = String.valueOf(doc.getMetadata().getOrDefault("title", "平台规则"));
            sb.append("\n--- 片段 ").append(i + 1).append("（出处：").append(title).append("）---\n")
              .append(doc.getText()).append("\n");
        }
        return AgentToolResult.of(sb.toString());
    }
}
