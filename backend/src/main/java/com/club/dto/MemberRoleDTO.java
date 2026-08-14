package com.club.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberRoleDTO {
    @NotBlank(message = "角色不能为空")
    private String memberRole;
}
