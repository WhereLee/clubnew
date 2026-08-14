package com.club;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;

import com.club.agent.AgentContext;
import com.club.agent.rag.KnowledgeLoader;
import com.club.agent.rag.KnowledgeSearchTool;

/**
 * RAG 知识库管线测试（独立组装，不依赖 Spring 条件装配与本地 ONNX 模型文件，CI 可跑）。
 * MockEmbeddingModel 用字符 2-gram 哈希向量：共享字词的文本向量相似度高，可验证语义检索链路。
 */
class RagKnowledgeTest {

    private SimpleVectorStore store;
    private KnowledgeSearchTool tool;

    private static final AgentContext STUDENT = new AgentContext(1001L, "stu1001", "林晓雨", "STUDENT", 4, null);

    @BeforeEach
    void setUp() {
        EmbeddingModel mock = new EmbeddingModel() {
            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                List<Embedding> embeddings = new ArrayList<>();
                int idx = 0;
                for (String text : request.getInstructions()) {
                    embeddings.add(new Embedding(ngramVector(text), idx++));
                }
                return new EmbeddingResponse(embeddings);
            }

            @Override
            public float[] embed(Document document) {
                return ngramVector(document.getText());
            }

            private float[] ngramVector(String text) {
                float[] v = new float[256];
                String t = (text == null ? "" : text).toLowerCase();
                for (int i = 0; i < t.length() - 1; i++) {
                    int h = (t.charAt(i) * 31 + t.charAt(i + 1)) % 256;
                    v[h] += 1.0f;
                }
                double norm = 0;
                for (float f : v) {
                    norm += f * f;
                }
                if (norm > 0) {
                    norm = Math.sqrt(norm);
                    for (int i = 0; i < v.length; i++) {
                        v[i] = (float) (v[i] / norm);
                    }
                }
                return v;
            }
        };
        store = SimpleVectorStore.builder(mock).build();
        store.add(KnowledgeLoader.loadFromClasspath());
        // toolLogMapper/objectMapper 传 null：execute 路径的轨迹落库为 try-catch 兜底，不影响测试
        tool = new KnowledgeSearchTool(null, null, store);
    }

    @Test
    void knowledgeBase_seededFromRagDocs() {
        List<Document> all = store.similaritySearch(
                SearchRequest.builder().query("平台").topK(1000).build());
        assert !all.isEmpty() : "知识库应有文档";
        long titles = all.stream().map(d -> d.getMetadata().get("title")).distinct().count();
        assert titles >= 3 : "至少 3 篇知识文档，实际 " + titles;
    }

    @Test
    void knowledgeSearch_hitsOnJoinFlow() {
        // 直接验证检索管线：embedding → 入库 → 相似度检索应命中入社相关文档
        List<Document> hits = store.similaritySearch(
                SearchRequest.builder().query("怎么加入社团").topK(4).similarityThreshold(0.0).build());
        assert !hits.isEmpty() : "检索应有命中";
        boolean hasJoinContent = hits.stream().anyMatch(d ->
                d.getText().contains("入社") || d.getText().contains("申请入社") || d.getText().contains("加入社团"));
        assert hasJoinContent : "命中内容应包含入社相关文本: " + hits.stream().map(Document::getText).toList();
    }

    @Test
    void knowledgeSearch_toolOnHittableQuery_returnsReferences() {
        // 工具层：查询包含文档原文关键词（n-gram 高重叠）时返回参考片段
        var result = tool.execute(Map.of("query", "报名成功 名额 取消报名"), STUDENT);
        assert result.content().contains("知识库参考片段") : result.content();
    }

    @Test
    void knowledgeSearch_hitsOnActivityCheckin() {
        var result = tool.execute(Map.of("query", "活动签到规则是什么"), STUDENT);
        assert result.content().contains("知识库参考片段") : result.content();
    }

    @Test
    void knowledgeSearch_emptyResult_graceful() {
        var result = tool.execute(Map.of("query", "量子力学相对论场论波函数"), STUDENT);
        // 要么检索命中（阈值内），要么优雅返回无结果文案，不允许抛异常
        assert result.content().contains("知识库参考片段") || result.content().contains("没有检索到");
    }

    @Test
    void chunker_keepsParagraphsIntact() {
        String text = "第一段内容。\n\n第二段内容。\n\n第三段内容。";
        List<Document> chunks = KnowledgeLoader.chunk(text, "测试");
        assert chunks.size() == 1 : "短文本应聚合为一块";
        assert chunks.get(0).getText().contains("第一段") && chunks.get(0).getText().contains("第三段");
    }
}
