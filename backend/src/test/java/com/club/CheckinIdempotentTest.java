package com.club;

import com.club.common.BusinessException;
import com.club.config.TestConfig;
import com.club.domain.*;
import com.club.enums.ActivityStatus;
import com.club.mapper.ActivityCheckinMapper;
import com.club.service.*;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class CheckinIdempotentTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityCheckinMapper checkinMapper;

    @Autowired
    private ClubService clubService;

    private Long activityId;
    private Long userId = 3001L;

    @BeforeEach
    void setup() {
        Club club = new Club();
        club.setName("签到测试社团" + System.nanoTime());
        club.setCategory("VOLUNTEER");
        club.setDescription("测试");
        clubService.applyClub(club, 1L);
        clubService.auditClub(club.getId(), true, "通过", 1L);
        Long clubId = clubService.getById(club.getId()).getId();

        Activity activity = new Activity();
        activity.setClubId(clubId);
        activity.setTitle("签到测试活动");
        activity.setQuota(50);
        activity.setStartTime(LocalDateTime.now().minusDays(1));
        activity.setEndTime(LocalDateTime.now().plusDays(1));
        activity.setCheckinEnabled("Y");
        activityService.createActivity(activity);
        activity.setStatus(ActivityStatus.PUBLISHED.name());
        activityService.updateById(activity);
        activityId = activity.getId();

        // 先报名
        activityService.signupActivity(activityId, userId);
    }

    @Test
    void duplicateCheckin_onlyOneRecord() {
        activityService.checkin(activityId, userId);
        activityService.checkin(activityId, userId); // 第二次应幂等

        LambdaQueryWrapper<ActivityCheckin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityCheckin::getActivityId, activityId).eq(ActivityCheckin::getUserId, userId);
        long count = checkinMapper.selectCount(wrapper);
        assertEquals(1, count, "签到记录应只有1条");
    }

    @Test
    void unregisteredUser_checkin_throwsException() {
        assertThrows(BusinessException.class,
                () -> activityService.checkin(activityId, 9999L));
    }

    @Test
    void checkinDisabled_throwsException() {
        // 创建一个未开启签到的活动
        Activity act = new Activity();
        act.setClubId(1L);
        act.setTitle("无签到活动");
        act.setQuota(10);
        act.setCheckinEnabled("N");
        act.setStatus(ActivityStatus.PUBLISHED.name());
        activityService.createActivity(act);

        assertThrows(BusinessException.class,
                () -> activityService.checkin(act.getId(), userId));
    }
}
