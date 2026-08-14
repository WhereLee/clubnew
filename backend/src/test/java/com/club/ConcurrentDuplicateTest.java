package com.club;

import com.club.config.TestConfig;
import com.club.service.ActivityService;
import com.club.service.RecruitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 并发重复报名 / 签到测试：验证同一用户并发重复操作时，唯一约束兜底保证只产生一条记录。
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class ConcurrentDuplicateTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private RecruitService recruitService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void concurrentDuplicateSignup_onlyOneRecordAndNoOverDeduct() throws Exception {
        // 造社团 + 纳新（IN_PROGRESS，时间窗口内，quota 充足）
        jdbcTemplate.update("INSERT INTO club (id, name, code, status, member_count, star_level, create_time, update_time, deleted) VALUES (5001,'并发纳新社团','C5001','APPROVED',0,0,NOW(),NOW(),0)");
        jdbcTemplate.update("INSERT INTO recruit (id, club_id, title, description, quota, applied_count, start_time, end_time, status, version, create_time, update_time, deleted) " +
                "VALUES (9001, 5001, '并发纳新', '测试', 100, 0, ?, ?, 'IN_PROGRESS', 0, NOW(), NOW(), 0)",
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));

        Long recruitId = 9001L;
        Long userId = 8001L;

        // 同一用户并发报名 30 次
        runConcurrently(30, () -> {
            try {
                recruitService.applyRecruit(recruitId, userId);
            } catch (Exception ignored) {
            }
        });

        // 只有 1 条报名记录
        Long recordCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM recruit_record WHERE recruit_id = ? AND user_id = ?", Long.class, recruitId, userId);
        assertEquals(1L, recordCount, "并发重复报名应只产生 1 条记录");

        // 名额只被扣 1 次（applied_count == 1）
        Integer appliedCount = jdbcTemplate.queryForObject(
                "SELECT applied_count FROM recruit WHERE id = ?", Integer.class, recruitId);
        assertEquals(1, appliedCount, "并发重复报名名额只应扣 1 次");
    }

    @Test
    void concurrentDuplicateCheckin_onlyOneRecord() throws Exception {
        // 造社团 + 活动（ONGOING，签到开启，时间窗口内）
        jdbcTemplate.update("INSERT INTO club (id, name, code, status, member_count, star_level, create_time, update_time, deleted) VALUES (5002,'并发签到社团','C5002','APPROVED',0,0,NOW(),NOW(),0)");
        jdbcTemplate.update("INSERT INTO activity (id, club_id, title, start_time, end_time, quota, applied_count, status, checkin_enabled, version, create_time, update_time, deleted) " +
                "VALUES (6001, 5002, '签到活动', ?, ?, 100, 1, 'ONGOING', 'Y', 0, NOW(), NOW(), 0)",
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));
        jdbcTemplate.update("INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, version, create_time, update_time, deleted) " +
                "VALUES (7001, 6001, 8002, 'SIGNED', NOW(), 0, NOW(), NOW(), 0)");

        Long activityId = 6001L;
        Long userId = 8002L;

        // 同一用户并发签到 30 次
        runConcurrently(30, () -> {
            try {
                activityService.checkin(activityId, userId);
            } catch (Exception ignored) {
            }
        });

        Long checkinCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM activity_checkin WHERE activity_id = ? AND user_id = ?", Long.class, activityId, userId);
        assertEquals(1L, checkinCount, "并发重复签到应只产生 1 条记录");
    }

    private void runConcurrently(int times, Runnable task) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(times);
        for (int i = 0; i < times; i++) {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    latch.countDown();
                }
            });
        }
        boolean finished = latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(finished, "并发任务应在超时内完成");
    }
}
