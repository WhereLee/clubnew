package com.club.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefreshDTO {

    @NotBlank(message = "refreshToken 不能为空")
    @Size(max = 128, message = "refreshToken 长度非法")
    private String refreshToken;
}
