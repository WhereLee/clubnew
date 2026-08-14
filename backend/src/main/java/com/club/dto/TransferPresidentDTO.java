package com.club.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferPresidentDTO {
    @NotNull(message = "社团ID不能为空")
    private Long clubId;
    @NotNull(message = "新社长用户ID不能为空")
    private Long newPresidentUserId;
}
