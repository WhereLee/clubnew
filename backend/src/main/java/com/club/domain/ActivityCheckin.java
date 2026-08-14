package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_checkin")
public class ActivityCheckin extends BaseEntity {
    private Long activityId;
    private Long userId;
    private LocalDateTime checkinTime;
    private String status;
}
