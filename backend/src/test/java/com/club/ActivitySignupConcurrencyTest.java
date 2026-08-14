package com.club;

import com.club.common.BusinessException;
import com.club.config.TestConfig;
import com.club.domain.Activity;
import com.club.domain.ActivitySignup;
import com.club.domain.Club;
import com.club.enums.ActivityStatus;
import com.club.service.ActivityService;
import com.club.service.ClubService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class ActivitySignupConcurrencyTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private ClubService clubService;

    private Long clubId;

    @BeforeEach
    void setup() {
        Club club = new Club();
        club.setName("活动测试社团" + System.nanoTime());
        club.setCategory("SPORTS");
        club.setDescription("测试");
        clubService.applyClub(club, 1L);
        clubService.auditClub(club.getId(), true, "通过", 1L);
        clubId = clubService.getById(club.getId()).getId();
    }

    @Test
    void activitySignup20concurrent_noOverSell() throws Exception {
        // quota=5, 20并发报名
        Activity activity = new Activity();
        activity.setClubId(clubId);
        activity.setTitle("测试活动");
        activity.setQuota(5);
        activity.setStartTime(LocalDateTime.now().minusDays(1));
        activity.setEndTime(LocalDateTime.now().plusDays(1));
        activity.setCheckinEnabled("Y");
        activityService.createActivity(activity);
        jdbcTemplate.update("UPDATE activity SET status = 'PUBLISHED' WHERE id = ?", activity.getId());

        Long activityId = activity.getId();
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final long userId = 2000L + i;
            executor.submit(() -> {
                try {
                    activityService.signupActivity(activityId, userId);
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        Activity updated = activityService.getById(activityId);
        assertEquals(5, updated.getAppliedCount(), "applied_count应为5");
        assertEquals(5, successCount.get());
        assertEquals(15, failCount.get());
    }
}
