package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 用户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String userType;
    private String status;

    /** 角色列表（非数据库字段） */
    @TableField(exist = false)
    private List<SysRole> roles;

    /** 角色ID列表（非数据库字段） */
    @TableField(exist = false)
    private List<Long> roleIds;
}
