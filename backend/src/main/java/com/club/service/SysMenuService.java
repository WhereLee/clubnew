package com.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.SysMenu;
import com.club.vo.RouterVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> list(String menuName, String status);

    /**
     * 根据用户ID查询权限集合
     */
    Set<String> selectPermsByUserId(Long userId);

    /**
     * 根据用户ID查询菜单（用于路由）
     */
    List<SysMenu> selectMenusByUserId(Long userId);

    /**
     * 构建菜单树
     */
    List<SysMenu> buildMenuTree(List<SysMenu> menus);

    /**
     * 构建路由树
     */
    List<RouterVO> buildRouterTree(List<SysMenu> menus);

    /**
     * 树形选择
     */
    List<Map<String, Object>> treeSelect();

    /**
     * 角色菜单树
     */
    Map<String, Object> roleMenuTreeSelect(Long roleId);
}
