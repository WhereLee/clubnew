package com.club.agent.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.club.agent.AgentContext;
import com.club.agent.dto.AgentChatRequest;
import com.club.agent.service.AgentContextFactory;
import com.club.agent.service.AgentServiceImpl;
import com.club.common.R;
import com.club.domain.AgentMessage;
import com.club.domain.AgentSession;
import com.club.security.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

/**
 * AI Agent 接口。
 *
 * SSE 事件协议（POST /agent/chat）：
 * - event=text  data=增量文本
 * - event=done  data={"sessionId":..,"messageId":..}
 */
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentServiceImpl agentService;
    private final AgentContextFactory contextFactory;

    /** 流式对话（SSE） */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@Valid @RequestBody AgentChatRequest request) {
        AgentContext ctx = contextFactory.build();
        return agentService.streamChat(request.getSessionId(), request.getContent(), ctx);
    }

    /** 我的会话列表 */
    @GetMapping("/sessions")
    public R<List<AgentSession>> sessions() {
        return R.success(agentService.listSessions(SecurityUtils.getUserId()));
    }

    /** 会话历史消息 */
    @GetMapping("/sessions/{id}/messages")
    public R<List<AgentMessage>> messages(@PathVariable Long id) {
        return R.success(agentService.listMessages(id, SecurityUtils.getUserId()));
    }

    /** 删除会话 */
    @DeleteMapping("/sessions/{id}")
    public R<Void> deleteSession(@PathVariable Long id) {
        agentService.deleteSession(id, SecurityUtils.getUserId());
        return R.success(null);
    }
}
