package com.club.controller;

import com.club.common.R;
import com.club.annotation.RateLimiter;
import com.club.domain.SysMenu;
import com.club.domain.SysUser;
import com.club.dto.LoginDTO;
import com.club.dto.RefreshDTO;
import com.club.dto.RegisterDTO;
import com.club.security.LoginUser;
import com.club.security.SecurityUtils;
import com.club.service.LoginService;
import com.club.service.RefreshTokenService;
import com.club.service.SysMenuService;
import com.club.service.SysRoleService;
import com.club.service.SysUserService;
import com.club.vo.LoginUserVO;
import com.club.vo.LoginVO;
import com.club.vo.RouterVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class AuthController {

    @Resource
    private LoginService loginService;

    @Resource
    private RefreshTokenService refreshTokenService;

    @Resource
    private SysUserService userService;

    @Resource
    private SysRoleService roleService;

    @Resource
    private SysMenuService menuService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/auth/register")
    public R<Void> register(@Valid @RequestBody RegisterDTO dto) {
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setUserType("STUDENT");
        user.setStatus("0");
        userService.addUser(user);
        // 默认分配学生角色（role_id=3，与种子数据一致）
        jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, 3)", user.getId());
        return R.success();
    }

    @PostMapping("/auth/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = loginService.login(dto.getUsername(), dto.getPassword());
        return R.success(vo);
    }

    /**
     * 刷新令牌：用 refresh token 换取新 access + 新 refresh（轮换）。
     * 未携带 access token（无需登录态），限流防爆破；复用检测在服务层完成。
     */
    @PostMapping("/auth/refresh")
    @RateLimiter(key = "auth:refresh", time = 60, count = 10)
    public R<LoginVO> refresh(@Valid @RequestBody RefreshDTO dto) {
        return R.success(refreshTokenService.refresh(dto.getRefreshToken()));
    }

    @PostMapping("/auth/logout")
    public R<Void> logout(@RequestHeader(value = "Authorization", required = false) String token,
                          @RequestBody(required = false) RefreshDTO dto) {
        String refreshToken = dto != null ? dto.getRefreshToken() : null;
        loginService.logout(token, refreshToken);
        return R.success();
    }

    @GetMapping("/getInfo")
    public R<LoginUserVO> getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = userService.getById(loginUser.getUserId());
        // 不返回密码
        user.setPassword(null);
        // 查询角色
        var roles = roleService.selectAll().stream()
                .filter(r -> loginUser.getPermissions() != null)
                .map(r -> r.getRoleKey())
                .collect(Collectors.toList());

        LoginUserVO vo = new LoginUserVO();
        vo.setUser(user);
        vo.setRoles(roles);
        vo.setPermissions(loginUser.getPermissions());
        return R.success(vo);
    }

    @GetMapping("/getRouters")
    public R<List<RouterVO>> getRouters() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        List<SysMenu> menus;
        if (loginUser.getUserType() != null && loginUser.getUserType().equals("ADMIN")) {
            // 管理员获取所有菜单
            menus = menuService.list(null, "0");
        } else {
            menus = menuService.selectMenusByUserId(loginUser.getUserId());
        }
        List<RouterVO> routers = menuService.buildRouterTree(menus);
        return R.success(routers);
    }
}
