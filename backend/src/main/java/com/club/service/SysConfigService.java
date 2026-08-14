package com.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.SysConfig;

public interface SysConfigService extends IService<SysConfig> {

    IPage<SysConfig> listPage(Integer pageNum, Integer pageSize, String configName, String configKey);

    /**
     * 按key取value(含缓存)
     */
    String getConfigValue(String configKey);
}
