package com.club.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Agent 工具调用轨迹（可解释性 + 审计）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("agent_tool_log")
public class AgentToolLog extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long sessionId;

    private Long messageId;

    private Long userId;

    private String toolName;

    private String arguments;

    private String resultSummary;

    private Long durationMs;

    /** 1 成功 / 0 失败 */
    private Integer status;
}
