package com.club;

import com.club.config.TestConfig;
import com.club.security.LoginUser;
import com.club.security.PermissionService;
import com.club.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class PermissionServiceTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private PermissionService permissionService;

    @Test
    void admin_hasAllPermissions() {
        // 模拟管理员登录
        LoginUser adminUser = new LoginUser(1L, "admin", "", "管理员", "ADMIN", Set.of("*:*:*"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(permissionService.hasPermi("system:user:list"));
        assertTrue(permissionService.hasPermi("any:permission"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void student_lacksAdminPermission() {
        // 模拟学生登录（没有system:user:list权限）
        LoginUser studentUser = new LoginUser(3L, "student", "", "学生", "STUDENT", Set.of("club:*"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(studentUser, null, studentUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertFalse(permissionService.hasPermi("system:user:list"));
        assertTrue(permissionService.hasPermi("club:*"));

        SecurityContextHolder.clearContext();
    }
}
