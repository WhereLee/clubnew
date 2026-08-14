package com.club.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RecruitDTO {
    private Long id;
    @NotNull(message = "社团ID不能为空")
    private Long clubId;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String description;
    @NotNull(message = "名额不能为空")
    @Min(value = 1, message = "名额必须大于0")
    private Integer quota;
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    private String requirements;
}
