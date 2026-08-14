package com.club.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityDTO {
    @NotNull(message = "社团ID不能为空")
    private Long clubId;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @Min(value = 1, message = "名额必须大于0")
    private Integer quota;
    private String checkinEnabled;
}
