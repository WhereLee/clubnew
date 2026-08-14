package com.club;

import com.club.common.BusinessException;
import com.club.config.TestConfig;
import com.club.service.LoginService;
import com.club.service.SysUserService;
import com.club.vo.LoginVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class AuthServiceTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private LoginService loginService;

    @Autowired
    private SysUserService userService;

    @Test
    void login_success_returnsToken() {
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        // void set 方法默认不做任何事，无需额外 stub

        LoginVO vo = loginService.login("admin", "admin123");
        assertNotNull(vo);
        assertNotNull(vo.getToken());
        assertTrue(vo.getExpiresIn() > 0);
    }

    @Test
    void login_wrongPassword_throwsException() {
        assertThrows(BusinessException.class, () -> loginService.login("admin", "wrongpassword"));
    }

    @Test
    void login_disabledUser_throwsException() {
        // 创建一个被禁用的测试用户
        com.club.domain.SysUser user = new com.club.domain.SysUser();
        user.setUsername("disabled_user");
        user.setPassword("test123");
        user.setNickname("禁用用户");
        user.setUserType("STUDENT");
        user.setStatus("1"); // 停用
        userService.addUser(user);

        assertThrows(BusinessException.class, () -> loginService.login("disabled_user", "test123"));
    }
}
