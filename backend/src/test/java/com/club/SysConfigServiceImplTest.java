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

        // 测试意图是「缓存未命中 → 查库 → 回填缓存」，断言与 DB 实际值一致，
        // 不依赖迁移种子文案（种子调整不应导致测试脆断）
        String dbValue = sysConfigService.getById(1L).getConfigValue();

        String result = sysConfigService.getConfigValue("sys.index.notice");

        assertEquals(dbValue, result);
        // 验证写入缓存（回填值与 DB 一致）
        verify(valueOps).set(eq("sys_config:sys.index.notice"), eq(dbValue), anyLong(), any());
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
