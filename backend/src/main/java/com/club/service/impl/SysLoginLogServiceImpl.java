package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.domain.SysLoginLog;
import com.club.mapper.SysLoginLogMapper;
import com.club.service.SysLoginLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    @Override
    public IPage<SysLoginLog> listPage(Integer pageNum, Integer pageSize, String userName, String status) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(userName)) {
            wrapper.like(SysLoginLog::getUserName, userName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysLoginLog::getStatus, status);
        }
        wrapper.orderByDesc(SysLoginLog::getLoginTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }
}
