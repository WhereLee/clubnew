package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 社团
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("club")
public class Club extends BaseEntity {

    private String name;
    private String code;
    private String logo;
    private String description;
    private String category;
    private Long advisorId;
    private Long presidentId;
    private String status;
    private Integer memberCount;
    private Integer starLevel;
    private Long createUserId;
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;
    private Long auditUserId;
    private String auditRemark;
}
