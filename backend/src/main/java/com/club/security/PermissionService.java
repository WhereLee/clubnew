package com.club.security;

import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 权限校验服务（@ss bean）
 */
@Service("ss")
public class PermissionService {

    /**
     * 判断当前用户是否拥有某个权限
     */
    public boolean hasPermi(String permission) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getPermissions() == null) {
            return false;
        }
        Set<String> perms = loginUser.getPermissions();
        return perms.contains("*:*:*") || perms.contains(permission);
    }

    /**
     * 判断当前用户是否拥有某个角色
     */
    public boolean hasRole(String roleKey) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return false;
        }
        // 管理员拥有所有角色
        if (loginUser.getUserType() != null && loginUser.getUserType().equals("ADMIN")) {
            return true;
        }
        // 简化实现：通过权限字符串判断
        return loginUser.getPermissions() != null && loginUser.getPermissions().contains("role:" + roleKey);
    }
}
