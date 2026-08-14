package com.club.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FundDTO {
    @NotNull(message = "社团ID不能为空")
    private Long clubId;
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotNull(message = "金额不能为空")
    @Min(value = 1, message = "金额必须大于0")
    private java.math.BigDecimal amount;
    @NotBlank(message = "类型不能为空")
    private String type;
}
