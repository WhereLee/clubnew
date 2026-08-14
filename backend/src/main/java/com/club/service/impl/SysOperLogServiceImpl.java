package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.domain.SysOperLog;
import com.club.mapper.SysOperLogMapper;
import com.club.service.SysOperLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    @Override
    public IPage<SysOperLog> listPage(Integer pageNum, Integer pageSize, String title, String operName, Integer status) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like(SysOperLog::getTitle, title);
        }
        if (StringUtils.hasText(operName)) {
            wrapper.like(SysOperLog::getOperName, operName);
        }
        if (status != null) {
            wrapper.eq(SysOperLog::getStatus, status);
        }
        wrapper.orderByDesc(SysOperLog::getOperTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }
}
