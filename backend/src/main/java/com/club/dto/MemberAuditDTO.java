package com.club.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberAuditDTO {
    @NotNull(message = "成员ID不能为空")
    private Long memberId;
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;
}
