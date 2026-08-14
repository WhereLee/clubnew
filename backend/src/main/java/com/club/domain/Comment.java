package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true) @TableName("comment")
public class Comment extends BaseEntity {
    private String bizType; private Long bizId; private Long userId;
    private String content; private Integer likeCount;
}
