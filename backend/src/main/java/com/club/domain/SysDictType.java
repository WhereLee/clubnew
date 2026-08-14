package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dict_type")
public class SysDictType extends BaseEntity {

    /** 字典名称 */
    private String dictName;

    /** 字典类型(唯一) */
    private String dictType;

    /** 状态(0正常 1停用) */
    private String status;
}
