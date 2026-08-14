package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true) @TableName("user_like")
public class UserLike extends BaseEntity {
    private String bizType; private Long bizId; private Long userId; private String status;
}
