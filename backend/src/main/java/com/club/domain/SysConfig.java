package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 参数配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {

    /** 参数名称 */
    private String configName;

    /** 参数键名 */
    private String configKey;

    /** 参数值 */
    private String configValue;

    /** 系统内置(Y/N) */
    private String configType;
}
