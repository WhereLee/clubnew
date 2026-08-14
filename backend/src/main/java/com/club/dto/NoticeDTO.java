package com.club.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoticeDTO {
    private Long clubId;
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;
}
