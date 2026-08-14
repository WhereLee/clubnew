package com.club.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 申请入社入参
 */
@Data
public class MemberApplyDTO {
    @NotNull(message = "社团ID不能为空")
    private Long clubId;
}
