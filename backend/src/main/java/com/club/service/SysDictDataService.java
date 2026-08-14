package com.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.SysDictData;

import java.util.List;

public interface SysDictDataService extends IService<SysDictData> {

    IPage<SysDictData> listPage(Integer pageNum, Integer pageSize, String dictType, String dictLabel);

    /**
     * 按字典类型查启用数据(含缓存)
     */
    List<SysDictData> getByType(String dictType);
}
