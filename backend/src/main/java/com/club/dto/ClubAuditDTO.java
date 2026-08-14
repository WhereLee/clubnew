package com.club.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClubAuditDTO {
    @NotNull(message = "社团ID不能为空")
    private Long clubId;
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;
    private String remark;
}
