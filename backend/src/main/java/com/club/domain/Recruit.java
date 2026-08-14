package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recruit")
public class Recruit extends BaseEntity {
    private Long clubId;
    private String title;
    private String description;
    private Integer quota;
    private Integer appliedCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String requirements;
    private Integer version;
}
