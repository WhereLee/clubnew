package com.club.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;

import reactor.core.publisher.Flux;

/**
 * Mock 对话模型：未配置 MIMO_API_KEY（api-key=mock）时启用。
 *
 * 用途：
 * 1. 本地无 key / CI 环境跑通全链路（会话、SSE 流式、工具注册、落库）；
 * 2. 流式输出模拟逐块返回，前端 SSE 渲染逻辑与真实模型完全一致。
 *
 * 能力边界（刻意保持简单）：不理解语义、不调用工具，只返回固定演示文案，
 * 并列出当前注册的工具清单——验证「工具清单已注入 system prompt」这一链路。
 */
public class MockChatModel implements ChatModel, StreamingChatModel {

    private static final String MOCK_PREFIX =
            "【Mock 模式】当前未配置 MIMO_API_KEY，由本地假模型应答。"
            + "配置环境变量 MIMO_API_KEY 后自动切换为小米 mimo-v2.5 真实模型。\n\n";

    @Override
    public ChatResponse call(Prompt prompt) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(buildReply(prompt)))));
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        String reply = buildReply(prompt);
        List<ChatResponse> chunks = new ArrayList<>();
        // 按固定步长切块，模拟真实模型的流式输出
        for (int i = 0; i < reply.length(); i += 12) {
            String piece = reply.substring(i, Math.min(i + 12, reply.length()));
            chunks.add(new ChatResponse(List.of(new Generation(new AssistantMessage(piece)))));
        }
        return Flux.fromIterable(chunks);
    }

    private String buildReply(Prompt prompt) {
        // 只取最后一条 user 消息（历史消息不参与 Mock 回显）
        String userText = prompt.getInstructions().stream()
                .filter(m -> m.getMessageType() == MessageType.USER)
                .map(Message::getText)
                .reduce((first, second) -> second)
                .orElse("");

        String systemText = prompt.getInstructions().stream()
                .filter(m -> m.getMessageType() == MessageType.SYSTEM)
                .map(Message::getText)
                .collect(Collectors.joining("\n"));

        // 从 system prompt 提取工具清单段（AgentService 生成的标准格式：### 可用工具 段）
        StringBuilder toolSection = new StringBuilder();
        int idx = systemText.indexOf("### 可用工具");
        if (idx >= 0) {
            String section = systemText.substring(idx);
            int end = section.indexOf("\n\n");
            toolSection.append(section, 0, end > 0 ? Math.min(end + 2, section.length()) : section.length());
        }

        return MOCK_PREFIX
                + "你刚才说：「" + userText + "」\n\n"
                + "我已收到。当前会话注册了以下工具：\n"
                + (toolSection.length() > 0 ? toolSection : "（无，工具清单将在各端 Phase B/C/D 挂载）")
                + "\n\n接入真实模型后，我会根据你的问题自主决定调用哪个工具。";
    }
}
