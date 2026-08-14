package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity")
public class Activity extends BaseEntity {
    private Long clubId;
    private String title;
    private String content;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer quota;
    private Integer appliedCount;
    private String status;
    private String checkinEnabled;
    private Integer version;
}
