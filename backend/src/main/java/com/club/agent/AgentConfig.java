package com.club.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Agent 编排配置。
 *
 * 模型选择策略（切换零代码）：
 * - api-key=mock（默认/未配置）：启用 MockChatModel（@Primary 优先于自动配置的 OpenAiChatModel）
 * - api-key=真实值：Mock 不装配，OpenAiChatModel（mimo，OpenAI 兼容协议）成为唯一 ChatModel
 *
 * ChatClient 是 Spring AI 的通用编排层，底层模型可换，业务代码不感知。
 */
@Configuration
public class AgentConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.ai.openai.api-key", havingValue = "mock", matchIfMissing = true)
    public ChatModel mockChatModel() {
        return new MockChatModel();
    }

    @Bean
    public ChatClient agentChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
