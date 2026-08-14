package com.club.agent.rag;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 知识库（RAG）配置。
 *
 * 设计决策：
 * 1. 向量存储用 SimpleVectorStore（JSON 文件持久化）——知识库 <1000 条，本地文件零外部依赖；
 *    VectorStore 是 Spring AI 抽象接口，后续切 pgvector（生产多实例）只改本类，业务零改动。
 * 2. embedding 模型路径经环境变量注入（BGE_MODEL_URI/BGE_TOKENIZER_URI）；
 *    未配置（CI/无模型环境）时 EmbeddingModel 不存在 → 本配置整体不生效 → RAG 工具不注册。
 * 3. 知识库在应用就绪后加载：目录为空才入库（幂等，重启不重复向量化）。
 */
@Slf4j
@Configuration
public class KnowledgeConfig {

    private final ObjectProvider<VectorStore> storeProvider;

    public KnowledgeConfig(ObjectProvider<VectorStore> storeProvider) {
        this.storeProvider = storeProvider;
    }

    @Value("${club.rag.store-path:./data/rag-store.json}")
    private String storePath;

    @Bean
    public VectorStore knowledgeVectorStore(ObjectProvider<EmbeddingModel> embeddingProvider) {
        EmbeddingModel embeddingModel = embeddingProvider.getIfAvailable();
        if (embeddingModel == null) {
            // 未配置 embedding 模型（CI/未注入 BGE_MODEL_URI）：注册禁用版，RAG 工具降级应答
            log.warn("EmbeddingModel 未配置：RAG 知识库降级为「未启用」状态。配置 BGE_MODEL_URI/BGE_TOKENIZER_URI 后自动开启。");
            return new DisabledVectorStore();
        }
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        Path path = Paths.get(storePath);
        if (Files.exists(path)) {
            store.load(path.toFile());
            log.info("知识库已从 {} 加载", storePath);
        }
        return store;
    }

    /** 应用就绪后检查知识库：为空则切块入库并持久化（禁用版跳过） */
    @EventListener(ApplicationReadyEvent.class)
    public void seedKnowledgeBase() {
        VectorStore knowledgeVectorStore = storeProvider.getIfAvailable();
        if (knowledgeVectorStore == null || knowledgeVectorStore instanceof DisabledVectorStore) {
            return;
        }
        try {
            if (!isEmpty(knowledgeVectorStore)) {
                return;
            }
            List<Document> docs = KnowledgeLoader.loadFromClasspath();
            if (docs.isEmpty()) {
                log.warn("未找到知识库文档（classpath:rag/*.md）");
                return;
            }
            knowledgeVectorStore.add(docs);
            File target = new File(storePath);
            if (target.getParentFile() != null) {
                target.getParentFile().mkdirs();
            }
            ((SimpleVectorStore) knowledgeVectorStore).save(target);
            log.info("知识库初始化完成：{} 块，已持久化到 {}", docs.size(), storePath);
        } catch (Exception e) {
            log.error("知识库初始化失败（RAG 检索将不可用）: {}", e.getMessage());
        }
    }

    private boolean isEmpty(VectorStore store) {
        try {
            return store.similaritySearch(
                    SearchRequest.builder().query("计数探针").topK(1000).build()).isEmpty();
        } catch (Exception e) {
            return true;
        }
    }
}
