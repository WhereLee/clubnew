package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_signup")
public class ActivitySignup extends BaseEntity {
    private Long activityId;
    private Long userId;
    private String status;
    private LocalDateTime signupTime;
    private Integer version;
}
