package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dict_data")
public class SysDictData extends BaseEntity {

    /** 排序 */
    private Integer dictSort;

    /** 标签(显示值) */
    private String dictLabel;

    /** 键值(存储值) */
    private String dictValue;

    /** 所属字典类型 */
    private String dictType;

    /** 是否默认(Y/N) */
    private String isDefault;

    /** 状态(0正常 1停用) */
    private String status;
}
