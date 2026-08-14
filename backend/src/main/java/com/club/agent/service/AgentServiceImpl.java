package com.club.agent.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.club.agent.AbstractAgentTool;
import com.club.agent.AgentContext;
import com.club.agent.AgentTool;
import com.club.agent.AgentToolRegistry;
import com.club.common.BusinessException;
import com.club.common.ResultCode;
import com.club.domain.AgentMessage;
import com.club.domain.AgentSession;
import com.club.mapper.AgentMessageMapper;
import com.club.mapper.AgentSessionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Agent 编排服务。
 *
 * SSE 事件协议：
 * - event=text：data=增量文本（前端直接追加渲染）
 * - event=done：data=JSON{sessionId, messageId}（流结束，assistant 消息已落库）
 *
 * 安全设计：
 * - 会话归属校验：只能访问自己的会话；
 * - 工具清单按当前用户权限过滤后才注入 system prompt；
 * - 工具真实调用由 ChatClient 自动编排（Phase B 起挂载各端工具）；
 * - 落库为「流结束后保存完整 assistant 消息」，流中断不落半截消息。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl {

    private static final int HISTORY_MESSAGE_LIMIT = 12;
    private static final int TITLE_MAX_LENGTH = 30;

    private final ChatClient agentChatClient;
    private final AgentToolRegistry toolRegistry;
    private final AgentSessionMapper sessionMapper;
    private final AgentMessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    /** 流式对话 */
    public Flux<ServerSentEvent<String>> streamChat(Long sessionId, String content, AgentContext ctx) {
        AgentSession session = resolveSession(sessionId, content, ctx);

        // 1. 用户消息落库
        AgentMessage userMsg = new AgentMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(content);
        messageMapper.insert(userMsg);

        // 2. 组装上下文：system prompt + 历史消息 + 当前消息
        List<Message> history = loadHistory(session.getId());

        // 3. 流式调用；聚合完整回复用于落库
        AtomicReference<StringBuilder> replyBuf = new AtomicReference<>(new StringBuilder());
        AtomicReference<StringBuilder> toolCallsBuf = new AtomicReference<>(new StringBuilder());
        AtomicLong assistantMsgId = new AtomicLong();

        Flux<ServerSentEvent<String>> textFlux = agentChatClient.prompt()
                .system(buildSystemPrompt(ctx))
                .messages(history)
                .user(content)
                // 按当前用户权限过滤后的工具集合（Spring AI 自动编排 function calling）
                // 注意：varargs 必须展开，否则 List 本身被当作单个工具对象扫描
                .tools(toolRegistry.toolsFor(ctx).toArray(new Object[0]))
                // 工具侧上下文：权限画像 + 会话 ID（工具轨迹落库用）
                .toolContext(Map.of(AbstractAgentTool.CTX_KEY, ctx, AbstractAgentTool.SESSION_KEY, session.getId()))
                .stream()
                .chatResponse()
                .doOnNext(cr -> collectToolCalls(cr, toolCallsBuf))
                .flatMapIterable(org.springframework.ai.chat.model.ChatResponse::getResults)
                .mapNotNull(gen -> gen.getOutput() == null ? null : gen.getOutput().getText())
                .filter(s -> !s.isEmpty())
                .doOnNext(chunk -> replyBuf.get().append(chunk))
                .map(chunk -> ServerSentEvent.<String>builder(chunk).event("text").build());

        // 4. 流结束后落库 assistant 消息，并发出 done 事件（携带落库后的 messageId）
        Mono<ServerSentEvent<String>> doneEvent = Mono.fromCallable(() -> {
            AgentMessage assistantMsg = new AgentMessage();
            assistantMsg.setSessionId(session.getId());
            assistantMsg.setRole("assistant");
            assistantMsg.setContent(replyBuf.get().toString());
            if (toolCallsBuf.get().length() > 0) {
                assistantMsg.setToolCalls(toolCallsBuf.get().toString());
            }
            messageMapper.insert(assistantMsg);
            assistantMsgId.set(assistantMsg.getId());

            // 更新会话标题（首条用户消息前 30 字）
            updateSessionTitle(session.getId(), content);

            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("sessionId", session.getId());
            meta.put("messageId", assistantMsg.getId());
            return ServerSentEvent.<String>builder(objectMapper.writeValueAsString(meta))
                    .event("done").build();
        });

        return textFlux.concatWith(doneEvent)
                .doOnError(e -> log.warn("agent 流式对话中断: {}", e.getMessage()));
    }

    /** 会话列表（当前用户） */
    public List<AgentSession> listSessions(Long userId) {
        return sessionMapper.selectList(new LambdaQueryWrapper<AgentSession>()
                .eq(AgentSession::getUserId, userId)
                .orderByDesc(AgentSession::getUpdateTime));
    }

    /** 历史消息（当前用户的会话） */
    public List<AgentMessage> listMessages(Long sessionId, Long userId) {
        checkSessionOwner(sessionId, userId);
        return messageMapper.selectList(new LambdaQueryWrapper<AgentMessage>()
                .eq(AgentMessage::getSessionId, sessionId)
                .orderByAsc(AgentMessage::getId));
    }

    /** 删除会话（逻辑删除） */
    public void deleteSession(Long sessionId, Long userId) {
        checkSessionOwner(sessionId, userId);
        sessionMapper.deleteById(sessionId);
    }

    // ---------------- 内部方法 ----------------

    /** 解析/新建会话，并校验归属 */
    private AgentSession resolveSession(Long sessionId, String content, AgentContext ctx) {
        if (sessionId == null) {
            AgentSession session = new AgentSession();
            session.setUserId(ctx.userId());
            session.setTitle(truncate(content));
            session.setModel("mimo-v2.5");
            sessionMapper.insert(session);
            return session;
        }
        checkSessionOwner(sessionId, ctx.userId());
        return sessionMapper.selectById(sessionId);
    }

    private void checkSessionOwner(Long sessionId, Long userId) {
        AgentSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该会话");
        }
    }

    /** 拉取会话最近 N 条历史消息（倒序取后反转） */
    private List<Message> loadHistory(Long sessionId) {
        List<AgentMessage> history = messageMapper.selectList(new LambdaQueryWrapper<AgentMessage>()
                .eq(AgentMessage::getSessionId, sessionId)
                .orderByDesc(AgentMessage::getId)
                .last("LIMIT " + HISTORY_MESSAGE_LIMIT));
        List<Message> messages = new ArrayList<>();
        for (int i = history.size() - 1; i >= 0; i--) {
            AgentMessage m = history.get(i);
            if (m.getContent() == null || m.getContent().isBlank()) {
                continue;
            }
            messages.add("user".equals(m.getRole())
                    ? new UserMessage(m.getContent())
                    : new AssistantMessage(m.getContent()));
        }
        return messages;
    }

    /** system prompt：角色设定 + 行为准则 + 权限过滤后的工具清单 + 当前用户画像 */
    private String buildSystemPrompt(AgentContext ctx) {
        String roleName = "ADMIN".equalsIgnoreCase(ctx.userType()) ? "系统管理员" : "学生用户";
        StringBuilder sb = new StringBuilder();
        sb.append("你是「社团全流程管理系统」的 AI 助手，服务对象是 ").append(roleName)
          .append(" ").append(ctx.nickname() == null ? "" : ctx.nickname()).append("。\n")
          .append("行为准则：\n")
          .append("1. 只使用「可用工具」获取信息，禁止编造任何数据；\n")
          .append("2. 一切写操作（发布、审批、修改数据）只输出建议，由用户本人确认后执行；\n")
          .append("3. 回答简洁、使用中文；数据类问题先调工具再回答；\n");
        if (ctx.isAdmin() || ctx.isClubAdmin()) {
            sb.append("4. 可为运营场景起草文案（纳新推文/活动公告/审批意见），标注「草稿」供用户修改后发布。\n\n");
        } else {
            sb.append("\n");
        }
        sb.append("### 可用工具\n");
        List<AgentTool> tools = toolRegistry.toolsFor(ctx);
        if (tools.isEmpty()) {
            sb.append("（暂无，各端工具将在后续版本挂载）\n");
        } else {
            for (AgentTool t : tools) {
                sb.append("- ").append(t.name()).append(": ").append(t.description()).append("\n");
            }
        }
        sb.append("\n### 当前用户\n")
          .append("- userId: ").append(ctx.userId()).append("\n")
          .append("- 角色: ").append(ctx.userType()).append("\n")
          .append("- 所属社团: ").append(ctx.clubId() == null ? "无" : ctx.clubId()).append("\n");
        return sb.toString();
    }

    /** 从流式 ChatResponse 中提取工具调用轨迹（JSON 数组拼接） */
    private void collectToolCalls(org.springframework.ai.chat.model.ChatResponse cr, AtomicReference<StringBuilder> buf) {
        try {
            for (var gen : cr.getResults()) {
                if (gen.getOutput() == null || gen.getOutput().getToolCalls().isEmpty()) {
                    continue;
                }
                String json = objectMapper.writeValueAsString(gen.getOutput().getToolCalls());
                buf.get().append(json);
            }
        } catch (Exception ignored) {
            // 轨迹聚合失败不影响主流程（工具级轨迹已在 AbstractAgentTool 落库）
        }
    }

    private void updateSessionTitle(Long sessionId, String firstContent) {        AgentSession session = new AgentSession();
        session.setId(sessionId);
        session.setTitle(truncate(firstContent));
        sessionMapper.updateById(session);
    }

    private String truncate(String text) {
        if (text == null) {
            return "新对话";
        }
        String oneLine = text.replaceAll("\\s+", " ").trim();
        return oneLine.length() <= TITLE_MAX_LENGTH ? oneLine : oneLine.substring(0, TITLE_MAX_LENGTH) + "…";
    }
}
