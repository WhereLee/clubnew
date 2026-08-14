package com.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.SysLoginLog;

public interface SysLoginLogService extends IService<SysLoginLog> {

    IPage<SysLoginLog> listPage(Integer pageNum, Integer pageSize, String userName, String status);
}
