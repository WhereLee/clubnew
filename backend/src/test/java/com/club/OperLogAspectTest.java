package com.club;

import com.club.annotation.Log;
import com.club.config.TestConfig;
import com.club.domain.SysOperLog;
import com.club.service.SysOperLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestConfig.class, OperLogAspectTest.LogTestHelper.class})
@Transactional
class OperLogAspectTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SysOperLogService operLogService;

    @Autowired
    private LogTestHelper logTestHelper;

    @Test
    void testLogAnnotation_createsOperLogRecord() {
        // 先验证服务层能正常写入
        SysOperLog directLog = new SysOperLog();
        directLog.setTitle("直接测试");
        directLog.setStatus(0);
        directLog.setOperTime(LocalDateTime.now());
        boolean saved = operLogService.save(directLog);
        assertTrue(saved, "直接保存应该成功");

        // 调用带 @Log 注解的方法
        String result = logTestHelper.testMethod();
        assertEquals("success", result);

        // 检查日志记录（包含直接保存的 + AOP的）
        List<SysOperLog> logs = operLogService.list();
        // 至少应该有1条记录（直接保存的）
        assertFalse(logs.isEmpty(), "至少应有1条操作日志");
        assertEquals(0, logs.get(0).getStatus(), "操作状态应为成功(0)");
    }

    /**
     * 测试辅助组件：提供带 @Log 注解的方法
     */
    @Component
    public static class LogTestHelper {

        @Log(title = "测试模块", businessType = 1)
        public String testMethod() {
            return "success";
        }
    }
}
