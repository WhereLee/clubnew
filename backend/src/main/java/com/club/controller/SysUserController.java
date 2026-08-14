package com.club.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.club.annotation.Log;
import com.club.common.BusinessException;
import com.club.common.R;
import com.club.domain.SysUser;
import com.club.security.SecurityUtils;
import com.club.service.SysUserService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Resource
    private SysUserService userService;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    public R<IPage<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String userName, String phone, String status) {
        IPage<SysUser> page = userService.listPage(pageNum, pageSize, userName, phone, status);
        // 清除密码
        page.getRecords().forEach(u -> u.setPassword(null));
        return R.success(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    public R<SysUser> getById(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user != null) user.setPassword(null);
        return R.success(user);
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @Log(title = "用户管理", businessType = 1)
    public R<Void> add(@RequestBody SysUser user) {
        userService.addUser(user);
        return R.success();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @Log(title = "用户管理", businessType = 2)
    public R<Void> update(@RequestBody SysUser user) {
        user.setPassword(null); // 修改时不更新密码
        userService.updateById(user);
        return R.success();
    }

    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @Log(title = "用户管理", businessType = 3)
    public R<Void> delete(@PathVariable String ids) {
        for (String idStr : ids.split(",")) {
            Long id = Long.parseLong(idStr);
            // 不能删除自己
            if (id.equals(SecurityUtils.getUserId())) {
                throw new BusinessException("不能删除当前用户");
            }
            // 不能删除超管
            SysUser user = userService.getById(id);
            if (user != null && "ADMIN".equals(user.getUserType())) {
                throw new BusinessException("不允许删除管理员用户");
            }
            userService.removeById(id);
        }
        return R.success();
    }

    @PutMapping("/resetPwd")
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @Log(title = "用户管理", businessType = 2)
    public R<Void> resetPwd(@RequestBody SysUser user) {
        userService.resetPassword(user.getId(), user.getPassword());
        return R.success();
    }

    @PutMapping("/changeStatus")
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @Log(title = "用户管理", businessType = 2)
    public R<Void> changeStatus(@RequestBody SysUser user) {
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setStatus(user.getStatus());
        userService.updateById(update);
        return R.success();
    }
}
