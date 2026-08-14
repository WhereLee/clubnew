package com.club.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI Agent 会话消息（user / assistant）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("agent_message")
public class AgentMessage extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long sessionId;

    /** user / assistant */
    private String role;

    private String content;

    /** assistant 消息的工具调用 JSON（编排轨迹，可空） */
    private String toolCalls;
}
