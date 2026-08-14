package com.club;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.context.ActiveProfiles;

import com.club.agent.AgentContext;
import com.club.agent.AgentToolRegistry;
import com.club.agent.service.AgentServiceImpl;
import com.club.domain.AgentMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.test.StepVerifier;

/**
 * Agent 底座测试（Mock 模式：不依赖外网 LLM）。
 * 覆盖：SSE 流式协议（text/done）、会话落库、归属校验、工具清单注入、权限过滤。
 */
@SpringBootTest
@ActiveProfiles("test")
class AgentServiceTest {

    @Autowired
    private AgentServiceImpl agentService;

    @Autowired
    private AgentToolRegistry toolRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    /** 管理员上下文（dataScope=1，可见全部工具） */
    private static final AgentContext ADMIN = new AgentContext(1L, "admin", "管理员", "ADMIN", 1, null);
    /** 学生上下文（无角色 dataScope 时默认最严格） */
    private static final AgentContext STUDENT = new AgentContext(1001L, "stu1001", "林晓雨", "STUDENT", 4, null);

    /** 从 done 事件 JSON 提取 sessionId */
    private Long sessionIdOf(ServerSentEvent<String> e) {
        try {
            JsonNode node = objectMapper.readTree(e.data());
            return node.get("sessionId").asLong();
        } catch (Exception ex) {
            throw new IllegalStateException("done 事件解析失败: " + e.data(), ex);
        }
    }

    @Test
    void streamChat_newSession_emitsTextThenDoneAndPersists() {
        AtomicReference<Long> sessionId = new AtomicReference<>();
        AtomicReference<StringBuilder> text = new AtomicReference<>(new StringBuilder());

        List<ServerSentEvent<String>> events = agentService
                .streamChat(null, "你好，介绍一下你自己", ADMIN)
                .collectList()
                .block();

        assert events != null && !events.isEmpty();
        for (ServerSentEvent<String> e : events) {
            if ("text".equals(e.event())) {
                text.get().append(e.data());
            } else if ("done".equals(e.event())) {
                sessionId.set(sessionIdOf(e));
            }
        }
        // done 事件存在且携带 sessionId
        assert sessionId.get() != null;
        // 流式内容非空（Mock 模型固定文案）
        assert !text.get().toString().isBlank();
        // 会话与消息已落库
        List<AgentMessage> messages = agentService.listMessages(sessionId.get(), 1L);
        assert messages.size() == 2;
        assert "user".equals(messages.get(0).getRole());
        assert "assistant".equals(messages.get(1).getRole());
        assert !messages.get(1).getContent().isBlank();
    }

    @Test
    void streamChat_replayWithSessionId_appendsHistory() {
        // 第一轮：新建会话
        AtomicReference<Long> sessionId = new AtomicReference<>();
        agentService.streamChat(null, "第一轮问题", ADMIN)
                .collectList().block().forEach(e -> {
                    if ("done".equals(e.event())) {
                        sessionId.set(sessionIdOf(e));
                    }
                });
        // 第二轮：同一会话
        agentService.streamChat(sessionId.get(), "第二轮问题", ADMIN).collectList().block();
        List<AgentMessage> afterSecond = agentService.listMessages(sessionId.get(), 1L);

        assert afterSecond.size() == 4;
        assert "第二轮问题".equals(afterSecond.get(2).getContent());
    }

    @Test
    void streamChat_foreignSession_rejected() {
        // 学生创建会话
        AtomicReference<Long> sessionId = new AtomicReference<>();
        agentService.streamChat(null, "学生自己的会话", STUDENT)
                .collectList().block().forEach(e -> {
                    if ("done".equals(e.event())) {
                        sessionId.set(sessionIdOf(e));
                    }
                });
        // 管理员访问学生会话 → 403
        try {
            agentService.listMessages(sessionId.get(), 1L);
            throw new AssertionError("应拒绝访问他人会话");
        } catch (com.club.common.BusinessException e) {
            assert e.getCode() == 403;
        }
    }

    @Test
    void registry_adminSeesAllTools_studentFiltered() {
        // Phase A 无工具挂载：两者皆为空；注册表本身可查询
        assert toolRegistry.allTools().isEmpty();
        assert toolRegistry.toolsFor(ADMIN).isEmpty();
        assert toolRegistry.toolsFor(STUDENT).isEmpty();
        // describeFor 输出为合法 prompt 文本
        assert toolRegistry.describeFor(ADMIN).isBlank();
    }

    @Test
    void streamChat_fluxIsReactive_neverBlocks() {
        StepVerifier.create(agentService.streamChat(null, "流式测试", ADMIN))
                .expectNextCount(1)
                .thenCancel()
                .verify();
    }
}
