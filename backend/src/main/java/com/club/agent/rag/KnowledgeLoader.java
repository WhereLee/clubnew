package com.club.agent.rag;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * 知识库加载与切块（独立静态工具，便于测试独立组装管线）。
 * 切块参数复用旧 RAG 项目验证过的段落聚合策略：500 字目标 + 100 重叠。
 */
public final class KnowledgeLoader {

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 100;

    private KnowledgeLoader() {
    }

    /** 加载 classpath:rag/*.md 并切块（标题作为元数据） */
    public static List<Document> loadFromClasspath() {
        List<Document> chunks = new ArrayList<>();
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath:rag/*.md");
            for (Resource res : resources) {
                String title = res.getFilename() == null ? "知识文档"
                        : res.getFilename().replace(".md", "");
                String text = new String(res.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                chunks.addAll(chunk(text, title));
            }
        } catch (Exception e) {
            throw new IllegalStateException("知识库文档加载失败", e);
        }
        return chunks;
    }

    /** 段落聚合切块（保证段落完整优先，超长段落硬切） */
    public static List<Document> chunk(String text, String title) {
        List<Document> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\\n\\s*\\n");
        StringBuilder buf = new StringBuilder();
        String prevTail = "";

        for (String p : paragraphs) {
            String clean = p.trim();
            if (clean.isEmpty()) {
                continue;
            }
            String candidate = prevTail + (prevTail.isEmpty() ? "" : "\n") + clean;
            if (buf.length() > 0 && buf.length() + candidate.length() > CHUNK_SIZE) {
                chunks.add(new Document(buf.toString(), Map.of("title", title)));
                String full = buf.toString();
                prevTail = full.length() > CHUNK_OVERLAP ? full.substring(full.length() - CHUNK_OVERLAP) : full;
                buf.setLength(0);
            } else if (buf.length() == 0 && candidate.length() > CHUNK_SIZE) {
                for (int i = 0; i < clean.length(); i += CHUNK_SIZE - CHUNK_OVERLAP) {
                    String piece = clean.substring(i, Math.min(i + CHUNK_SIZE, clean.length()));
                    chunks.add(new Document(piece, Map.of("title", title)));
                    if (i + CHUNK_SIZE >= clean.length()) {
                        break;
                    }
                }
                prevTail = "";
                continue;
            }
            buf.append(candidate);
        }
        if (buf.length() > 0) {
            chunks.add(new Document(buf.toString(), Map.of("title", title)));
        }
        return chunks;
    }
}
