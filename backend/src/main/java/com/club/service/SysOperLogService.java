package com.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.SysOperLog;

public interface SysOperLogService extends IService<SysOperLog> {

    IPage<SysOperLog> listPage(Integer pageNum, Integer pageSize, String title, String operName, Integer status);
}
