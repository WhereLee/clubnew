package com.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    IPage<SysRole> listPage(Integer pageNum, Integer pageSize, String roleName, String status);

    List<SysRole> selectAll();

    /**
     * 分配菜单权限
     */
    void insertRoleMenu(Long roleId, List<Long> menuIds);
}
