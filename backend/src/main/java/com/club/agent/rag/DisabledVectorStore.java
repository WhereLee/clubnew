package com.club.agent.rag;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

/**
 * 知识库禁用占位实现：未配置 embedding 模型（CI/未注入 BGE_MODEL_URI）时注册，
 * 保证 VectorStore 注入点始终有实现，RAG 工具以「未启用」文案降级应答。
 */
public class DisabledVectorStore implements VectorStore {

    @Override
    public void add(List<Document> documents) {
        // 禁用状态：忽略写入
    }

    @Override
    public void delete(List<String> idList) {
        // 禁用状态：忽略删除
    }

    @Override
    public void delete(Filter.Expression filterExpression) {
        // 禁用状态：忽略删除
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        return List.of();
    }
}
