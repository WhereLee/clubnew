package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true) @TableName("post")
public class Post extends BaseEntity {
    private Long clubId; private Long authorId; private String content;
    private Integer likeCount; private Integer commentCount; private String status;
}
