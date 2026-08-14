package com.club.controller;

import com.club.annotation.Log;
import com.club.common.R;
import com.club.domain.SysMenu;
import com.club.service.SysMenuService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    @Resource
    private SysMenuService menuService;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    public R<List<SysMenu>> list(String menuName, String status) {
        List<SysMenu> menus = menuService.list(menuName, status);
        return R.success(menus);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    public R<SysMenu> getById(@PathVariable Long id) {
        return R.success(menuService.getById(id));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @Log(title = "菜单管理", businessType = 1)
    public R<Void> add(@RequestBody SysMenu menu) {
        menuService.save(menu);
        return R.success();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @Log(title = "菜单管理", businessType = 2)
    public R<Void> update(@RequestBody SysMenu menu) {
        menuService.updateById(menu);
        return R.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @Log(title = "菜单管理", businessType = 3)
    public R<Void> delete(@PathVariable Long id) {
        // 检查是否有子菜单
        long childCount = menuService.list().stream()
                .filter(m -> id.equals(m.getParentId()))
                .count();
        if (childCount > 0) {
            return R.fail("存在子菜单，不允许删除");
        }
        menuService.removeById(id);
        return R.success();
    }

    @GetMapping("/treeselect")
    public R<List<Map<String, Object>>> treeSelect() {
        return R.success(menuService.treeSelect());
    }

    @GetMapping("/roleMenuTreeselect/{roleId}")
    public R<Map<String, Object>> roleMenuTreeselect(@PathVariable Long roleId) {
        return R.success(menuService.roleMenuTreeSelect(roleId));
    }
}
