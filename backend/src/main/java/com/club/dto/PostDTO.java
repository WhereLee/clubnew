package com.club.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostDTO {
    private Long clubId;
    @NotBlank(message = "内容不能为空")
    private String content;
}
