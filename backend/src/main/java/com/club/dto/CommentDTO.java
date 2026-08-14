package com.club.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentDTO {
    @NotBlank(message = "业务类型不能为空")
    private String bizType;
    @NotNull(message = "业务ID不能为空")
    private Long bizId;
    @NotBlank(message = "内容不能为空")
    private String content;
}
