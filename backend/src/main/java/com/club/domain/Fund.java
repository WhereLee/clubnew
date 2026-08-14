package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @EqualsAndHashCode(callSuper = true) @TableName("fund")
public class Fund extends BaseEntity {
    private Long clubId; private String title; private BigDecimal amount;
    private String type; private String status; private Long applyUserId;
    private Long auditUserId; private LocalDateTime auditTime; private String auditRemark;
}
