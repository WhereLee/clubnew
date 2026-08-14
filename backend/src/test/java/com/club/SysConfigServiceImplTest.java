package com.club;

import com.club.config.TestConfig;
import com.club.domain.SysConfig;
import com.club.service.SysConfigService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class SysConfigServiceImplTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SysConfigService sysConfigService;

    private ValueOperations<String, String> valueOps;

    @BeforeEach
    void setup() {
        valueOps = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void getConfigValue_cacheHit_returnCachedValue() {
        // 缓存命中
        when(valueOps.get("sys_config:sys.index.notice")).thenReturn("缓存中的值");

        String result = sysConfigService.getConfigValue("sys.index.notice");

        assertEquals("缓存中的值", result);
        verify(valueOps).get("sys_config:sys.index.notice");
    }

    @Test
    void getConfigValue_cacheMiss_queryDbAndCache() {
        // 缓存未命中
        when(valueOps.get("sys_config:sys.index.notice")).thenReturn(null);

        String result = sysConfigService.getConfigValue("sys.index.notice");

        assertEquals("欢迎使用社团管理系统", result);
        // 验证写入缓存
        verify(valueOps).set(eq("sys_config:sys.index.notice"), eq("欢迎使用社团管理系统"), anyLong(), any());
    }

    @Test
    void afterUpdate_thenCacheEvicted() {
        SysConfig config = sysConfigService.getById(1L);
        assertNotNull(config);
        config.setConfigValue("新的公告内容");
        sysConfigService.updateById(config);

        // 验证缓存被删除
        verify(stringRedisTemplate).delete("sys_config:sys.index.notice");
    }
}
