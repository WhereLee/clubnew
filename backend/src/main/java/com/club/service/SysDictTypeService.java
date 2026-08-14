package com.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.SysDictType;

public interface SysDictTypeService extends IService<SysDictType> {

    IPage<SysDictType> listPage(Integer pageNum, Integer pageSize, String dictName);

    /**
     * 按类型查询时清除缓存
     */
    void clearCache(String dictType);
}
