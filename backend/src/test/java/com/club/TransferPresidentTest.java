package com.club;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.club.common.BusinessException;
import com.club.config.TestConfig;
import com.club.domain.Club;
import com.club.domain.ClubMember;
import com.club.enums.MemberRole;
import com.club.service.ClubMemberService;
import com.club.service.ClubService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 换届测试：验证 Redisson 分布式锁被真实使用（tryLock 失败抛异常、成功走业务），
 * 以及换届后角色正确切换。
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class TransferPresidentTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private RedissonClient redissonClient;

    @Autowired
    private ClubService clubService;

    @Autowired
    private ClubMemberService clubMemberService;

    /** 造一个已审批通过的社团（社长=1L） */
    private Club createApprovedClub() {
        Club club = new Club();
        club.setName("换届测试" + System.nanoTime());
        club.setCategory("SPORTS");
        club.setDescription("测试");
        clubService.applyClub(club, 1L);
        clubService.auditClub(club.getId(), true, "通过", 1L);
        return clubService.getById(club.getId());
    }

    /** 添加并审批通过一个候选成员 */
    private Long addApprovedMember(Long clubId) {
        Long userId = 600L + Math.abs(System.nanoTime() % 100000);
        clubMemberService.applyMember(clubId, userId);
        LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubMember::getClubId, clubId).eq(ClubMember::getUserId, userId);
        ClubMember member = clubMemberService.getOne(wrapper);
        clubMemberService.auditMember(member.getId(), true, 1L);
        return userId;
    }

    /** 默认 stub：锁可获取 */
    private void stubLock(boolean acquired) {
        RLock mockLock = mock(RLock.class);
        when(redissonClient.getLock(anyString())).thenReturn(mockLock);
        try {
            when(mockLock.tryLock(anyLong(), anyLong(), any())).thenReturn(acquired);
        } catch (InterruptedException ignored) {
        }
        when(mockLock.isHeldByCurrentThread()).thenReturn(acquired);
    }

    @Test
    void transferPresident_success_switchesRoles() {
        stubLock(true);
        Club club = createApprovedClub();
        Long newUserId = addApprovedMember(club.getId());

        clubService.transferPresident(club.getId(), newUserId);

        // 原社长(1L)变为普通成员
        LambdaQueryWrapper<ClubMember> oldWrapper = new LambdaQueryWrapper<>();
        oldWrapper.eq(ClubMember::getClubId, club.getId()).eq(ClubMember::getUserId, 1L);
        ClubMember oldPresident = clubMemberService.getOne(oldWrapper);
        assertEquals(MemberRole.MEMBER.name(), oldPresident.getMemberRole());

        // 新社长角色正确
        LambdaQueryWrapper<ClubMember> newWrapper = new LambdaQueryWrapper<>();
        newWrapper.eq(ClubMember::getClubId, club.getId()).eq(ClubMember::getUserId, newUserId);
        ClubMember newPresident = clubMemberService.getOne(newWrapper);
        assertEquals(MemberRole.PRESIDENT.name(), newPresident.getMemberRole());

        // 社团 presidentId 已更新
        Club updated = clubService.getById(club.getId());
        assertEquals(newUserId, updated.getPresidentId());
    }

    @Test
    void transferPresident_lockHeldByOther_throwsException() {
        // 锁被其他线程占用：tryLock 返回 false，必须抛「换届操作进行中」
        stubLock(false);
        Club club = createApprovedClub();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> clubService.transferPresident(club.getId(), 999L));
        assertTrue(ex.getMessage().contains("换届操作进行中"), "应提示换届操作进行中");
    }

    @Test
    void transferPresident_nonMemberCannotBePresident() {
        stubLock(true);
        Club club = createApprovedClub();

        assertThrows(BusinessException.class,
                () -> clubService.transferPresident(club.getId(), 999L));
    }
}
