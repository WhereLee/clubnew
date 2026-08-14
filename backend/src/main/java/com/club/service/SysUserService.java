package com.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.SysUser;

public interface SysUserService extends IService<SysUser> {

    IPage<SysUser> listPage(Integer pageNum, Integer pageSize, String userName, String phone, String status);

    /**
     * 根据用户名查询
     */
    SysUser getByUsername(String username);

    /**
     * 新增用户（加密密码）
     */
    boolean addUser(SysUser user);

    /**
     * 重置密码
     */
    boolean resetPassword(Long userId, String password);
}
