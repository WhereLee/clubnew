package com.club.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivityAuditDTO {
    @NotBlank(message = "审核结果不能为空")
    private Boolean approved;
    private String remark;
}
