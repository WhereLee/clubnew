package com.club.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.club.annotation.Log;
import com.club.common.R;
import com.club.domain.SysRole;
import com.club.service.SysRoleService;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Resource
    private SysRoleService roleService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    public R<IPage<SysRole>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String roleName, String status) {
        return R.success(roleService.listPage(pageNum, pageSize, roleName, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    public R<SysRole> getById(@PathVariable Long id) {
        return R.success(roleService.getById(id));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @Log(title = "角色管理", businessType = 1)
    public R<Void> add(@RequestBody SysRole role) {
        roleService.save(role);
        // 保存角色菜单关联
        if (role.getMenuIds() != null && !role.getMenuIds().isEmpty()) {
            roleService.insertRoleMenu(role.getId(), role.getMenuIds());
        }
        return R.success();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @Log(title = "角色管理", businessType = 2)
    public R<Void> update(@RequestBody SysRole role) {
        roleService.updateById(role);
        if (role.getMenuIds() != null) {
            roleService.insertRoleMenu(role.getId(), role.getMenuIds());
        }
        return R.success();
    }

    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @Log(title = "角色管理", businessType = 3)
    public R<Void> delete(@PathVariable String ids) {
        for (String idStr : ids.split(",")) {
            Long roleId = Long.parseLong(idStr);
            // 检查是否已分配用户
            Long count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sys_user_role WHERE role_id = ?", Long.class, roleId);
            if (count != null && count > 0) {
                return R.fail("该角色已分配用户，不允许删除");
            }
            roleService.removeById(roleId);
            // 清除角色菜单关联
            jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_id = ?", roleId);
        }
        return R.success();
    }

    @GetMapping("/optionselect")
    public R<List<SysRole>> optionselect() {
        return R.success(roleService.selectAll());
    }
}
