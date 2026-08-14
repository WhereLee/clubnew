package com.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.club.domain.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON rm.menu_id = m.id " +
            "INNER JOIN sys_user_role ur ON ur.role_id = rm.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = '0' AND m.deleted = 0 " +
            "AND m.menu_type IN ('M', 'C') " +
            "ORDER BY m.parent_id, m.order_num")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM sys_menu WHERE status = '0' AND deleted = 0 ORDER BY parent_id, order_num")
    List<SysMenu> selectMenusAll();

    @Select("SELECT DISTINCT m.perms FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON rm.menu_id = m.id " +
            "INNER JOIN sys_user_role ur ON ur.role_id = rm.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = '0' AND m.deleted = 0 " +
            "AND m.perms IS NOT NULL AND m.perms != ''")
    List<String> selectPermsByUserId(@Param("userId") Long userId);
}
