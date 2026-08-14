package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.domain.SysRole;
import com.club.domain.SysUser;
import com.club.mapper.SysRoleMapper;
import com.club.service.SysRoleService;
import com.club.service.SysUserService;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private SysUserService userService;

    @Override
    public IPage<SysRole> listPage(Integer pageNum, Integer pageSize, String roleName, String status) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(roleName)) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysRole::getStatus, status);
        }
        wrapper.orderByAsc(SysRole::getRoleSort);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<SysRole> selectAll() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, "0");
        wrapper.orderByAsc(SysRole::getRoleSort);
        return list(wrapper);
    }

    @Override
    @Transactional
    public void insertRoleMenu(Long roleId, List<Long> menuIds) {
        // 先删除原有关联
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_id = ?", roleId);
        // 批量插入
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                jdbcTemplate.update("INSERT INTO sys_role_menu (role_id, menu_id) VALUES (?, ?)", roleId, menuId);
            }
        }
    }
}
