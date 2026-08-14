package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.domain.SysDictType;
import com.club.mapper.SysDictTypeMapper;
import com.club.service.SysDictTypeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    @Override
    public IPage<SysDictType> listPage(Integer pageNum, Integer pageSize, String dictName) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dictName)) {
            wrapper.like(SysDictType::getDictName, dictName);
        }
        wrapper.orderByAsc(SysDictType::getId);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void clearCache(String dictType) {
        // 由调用方负责清缓存（本阶段在controller或切面中处理）
    }
}
