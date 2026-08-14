package com.club;

import com.club.annotation.RepeatSubmit;
import com.club.common.BusinessException;
import com.club.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestConfig.class, RepeatSubmitAspectTest.RepeatSubmitTestHelper.class})
class RepeatSubmitAspectTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RepeatSubmitTestHelper helper;

    private ValueOperations<String, String> valueOps;

    @BeforeEach
    void setup() {
        valueOps = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void whenRedisReturnsFalse_thenThrowBusinessException() {
        // 模拟 Redis setIfAbsent 返回 false（重复提交）
        when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false);

        assertThrows(BusinessException.class, () -> helper.repeatTestMethod("test"));
    }

    @Test
    void whenRedisReturnsTrue_thenSucceed() {
        // 模拟 Redis setIfAbsent 返回 true（首次提交）
        when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);

        assertDoesNotThrow(() -> helper.repeatTestMethod("test"));
    }

    /**
     * 测试辅助组件：提供带 @RepeatSubmit 注解的方法
     */
    @org.springframework.stereotype.Component
    public static class RepeatSubmitTestHelper {

        @RepeatSubmit(interval = 5000)
        public String repeatTestMethod(String param) {
            return "success";
        }
    }
}
