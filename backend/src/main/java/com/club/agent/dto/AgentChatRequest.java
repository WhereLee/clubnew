package com.club.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

/**
 * Agent 对话请求
 */
@Data
public class AgentChatRequest {

    /** 会话 ID；空则新建会话 */
    private Long sessionId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息最长 2000 字")
    private String content;
}
