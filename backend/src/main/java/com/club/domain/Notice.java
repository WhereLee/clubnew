package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data @EqualsAndHashCode(callSuper = true) @TableName("notice")
public class Notice extends BaseEntity {
    private Long clubId; private String title; private String content;
    private Long publishUserId; private LocalDateTime publishTime;
    private String status; private String top;
}
