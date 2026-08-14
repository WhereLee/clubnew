package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单/权限
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    private String menuName;
    private Long parentId;
    private Integer orderNum;
    private String path;
    private String component;
    private String perms;
    private String menuType;
    private String icon;
    private String status;

    /** 子菜单列表（非数据库字段） */
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();
}
