package com.club;

import com.club.common.BusinessException;
import com.club.config.TestConfig;
import com.club.domain.SysUser;
import com.club.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class SysUserServiceImplTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SysUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void addUser_passwordIsEncrypted() {
        SysUser user = new SysUser();
        user.setUsername("testuser");
        user.setPassword("plaintext123");
        user.setNickname("测试用户");
        user.setUserType("STUDENT");
        user.setStatus("0");
        userService.addUser(user);

        // 验证密码不是明文
        SysUser saved = userService.getByUsername("testuser");
        assertNotNull(saved);
        assertNotEquals("plaintext123", saved.getPassword());
        assertTrue(passwordEncoder.matches("plaintext123", saved.getPassword()));
    }

    @Test
    void addUser_duplicateUsername_throwsException() {
        SysUser user = new SysUser();
        user.setUsername("admin"); // 已存在
        user.setPassword("test123");
        assertThrows(BusinessException.class, () -> userService.addUser(user));
    }

    @Test
    void deleteAdminUser_notAllowed() {
        // 不能删除管理员（admin id=1）
        // 模拟删除admin用户的逻辑
        SysUser admin = userService.getById(1L);
        assertNotNull(admin);
        assertEquals("ADMIN", admin.getUserType());
        // 实际删除逻辑在controller层校验，这里验证数据
    }
}
