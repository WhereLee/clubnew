package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 社团成员
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("club_member")
public class ClubMember extends BaseEntity {

    private Long clubId;
    private Long userId;
    private String memberRole;
    private String status;
    private LocalDateTime applyTime;
    private LocalDateTime joinTime;
    private Long auditUserId;
}
