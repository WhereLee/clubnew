package com.club;

import com.club.common.BusinessException;
import com.club.config.TestConfig;
import com.club.domain.Activity;
import com.club.domain.ActivityCheckin;
import com.club.domain.ActivitySignup;
import com.club.domain.Club;
import com.club.domain.Recruit;
import com.club.domain.RecruitRecord;
import com.club.enums.ActivityStatus;
import com.club.enums.RecruitRecordStatus;
import com.club.enums.RecruitStatus;
import com.club.mapper.RecruitMapper;
import com.club.mapper.RecruitRecordMapper;
import com.club.service.ActivityService;
import com.club.service.ClubService;
import com.club.service.RecruitService;
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
class SeckillConcurrencyTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RecruitService recruitService;

    @Autowired
    private RecruitRecordMapper recruitRecordMapper;

    @Autowired
    private RecruitMapper recruitMapper;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private ClubService clubService;

    private Long clubId;

    @BeforeEach
    void setup() {
        // 创建并审批一个社团
        Club club = new Club();
        club.setName("纳新测试社团" + System.nanoTime());
        club.setCategory("ACADEMIC");
        club.setDescription("测试");
        clubService.applyClub(club, 1L);
        clubService.auditClub(club.getId(), true, "通过", 1L);
        clubId = clubService.getById(club.getId()).getId();
    }

    @Test
    void seckill100concurrent_noOverSell() throws Exception {
        // 创建纳新：quota=10
        Recruit recruit = new Recruit();
        recruit.setClubId(clubId);
        recruit.setTitle("测试纳新");
        recruit.setQuota(10);
        recruit.setStartTime(LocalDateTime.now().minusDays(1));
        recruit.setEndTime(LocalDateTime.now().plusDays(1));
        recruitService.createRecruit(recruit);
        // 使用JDBC直接更新状态
        jdbcTemplate.update("UPDATE recruit SET status = 'IN_PROGRESS' WHERE id = ?", recruit.getId());

        Long recruitId = recruit.getId();

        // 验证数据库状态
        Recruit dbRecruit = recruitService.getById(recruitId);
        assertNotNull(dbRecruit, "纳新应存在");
        assertEquals("IN_PROGRESS", dbRecruit.getStatus(), "状态应为IN_PROGRESS");
        assertEquals(0, dbRecruit.getAppliedCount(), "初始报名数应为0");
        assertEquals(0, dbRecruit.getVersion(), "初始版本应为0");

        // 100并发报名
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final long userId = 1000L + i;
            executor.submit(() -> {
                try {
                    recruitService.applyRecruit(recruitId, userId);
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

        // 验证：恰好10人成功
        Recruit updated = recruitService.getById(recruitId);
        assertEquals(10, updated.getAppliedCount(), "applied_count应为10");

        LambdaQueryWrapper<RecruitRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecruitRecord::getRecruitId, recruitId)
               .eq(RecruitRecord::getStatus, RecruitRecordStatus.PENDING.name());
        long recordCount = recruitRecordMapper.selectCount(wrapper);
        assertEquals(10, recordCount, "报名记录应为10条");
        assertEquals(10, successCount.get(), "成功数应为10");
        assertEquals(90, failCount.get(), "失败数应为90");
    }
}
