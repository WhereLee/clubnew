package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recruit_record")
public class RecruitRecord extends BaseEntity {
    private Long recruitId;
    private Long userId;
    private String status;
    private LocalDateTime applyTime;
    private LocalDateTime interviewTime;
    private String interviewResult;
    private Integer version;
}
