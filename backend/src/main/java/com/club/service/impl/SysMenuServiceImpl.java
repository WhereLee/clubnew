package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.domain.SysMenu;
import com.club.mapper.SysMenuMapper;
import com.club.service.SysMenuService;
import com.club.vo.RouterVO;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Resource
    private SysMenuMapper menuMapper;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<SysMenu> list(String menuName, String status) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(menuName)) {
            wrapper.like(SysMenu::getMenuName, menuName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysMenu::getStatus, status);
        }
        wrapper.orderByAsc(SysMenu::getParentId, SysMenu::getOrderNum);
        return list(wrapper);
    }

    @Override
    public Set<String> selectPermsByUserId(Long userId) {
        List<String> perms = menuMapper.selectPermsByUserId(userId);
        return new HashSet<>(perms != null ? perms : Collections.emptyList());
    }

    @Override
    public List<SysMenu> selectMenusByUserId(Long userId) {
        return menuMapper.selectMenusByUserId(userId);
    }

    @Override
    public List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        Map<Long, SysMenu> menuMap = menus.stream()
                .collect(Collectors.toMap(SysMenu::getId, m -> m));
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId() == null || menu.getParentId() == 0L) {
                tree.add(menu);
            } else {
                SysMenu parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    parent.getChildren().add(menu);
                }
            }
        }
        return tree;
    }

    @Override
    public List<RouterVO> buildRouterTree(List<SysMenu> menus) {
        List<SysMenu> tree = buildMenuTree(menus);
        return tree.stream().map(this::convertRouter).collect(Collectors.toList());
    }

    private RouterVO convertRouter(SysMenu menu) {
        RouterVO router = new RouterVO();
        router.setName(menu.getPath());
        router.setPath("/" + menu.getPath());
        router.setComponent(menu.getComponent());
        router.setIcon(menu.getIcon());
        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            router.setChildren(menu.getChildren().stream().map(this::convertRouter).collect(Collectors.toList()));
        }
        return router;
    }

    @Override
    public List<Map<String, Object>> treeSelect() {
        List<SysMenu> menus = menuMapper.selectMenusAll();
        List<SysMenu> tree = buildMenuTree(menus);
        return tree.stream().map(this::convertTreeSelect).collect(Collectors.toList());
    }

    private Map<String, Object> convertTreeSelect(SysMenu menu) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", menu.getId());
        map.put("label", menu.getMenuName());
        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            map.put("children", menu.getChildren().stream().map(this::convertTreeSelect).collect(Collectors.toList()));
        }
        return map;
    }

    @Override
    public Map<String, Object> roleMenuTreeSelect(Long roleId) {
        List<SysMenu> menus = menuMapper.selectMenusAll();
        List<Map<String, Object>> tree = treeSelect();
        // 查询角色已选菜单
        List<Long> checkedKeys = jdbcTemplate.queryForList(
                "SELECT menu_id FROM sys_role_menu WHERE role_id = ?", Long.class, roleId);
        Map<String, Object> result = new HashMap<>();
        result.put("menus", tree);
        result.put("checkedKeys", checkedKeys);
        return result;
    }
}
