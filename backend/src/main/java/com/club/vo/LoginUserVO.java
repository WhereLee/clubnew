package com.club.vo;

import com.club.domain.SysUser;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class LoginUserVO {
    private SysUser user;
    private List<String> roles;
    private Set<String> permissions;
}
