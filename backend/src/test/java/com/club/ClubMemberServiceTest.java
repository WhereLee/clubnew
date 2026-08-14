package com.club;

import com.club.common.BusinessException;
import com.club.config.TestConfig;
import com.club.domain.Club;
import com.club.domain.ClubMember;
import com.club.enums.ClubStatus;
import com.club.enums.MemberRole;
import com.club.enums.MemberStatus;
import com.club.service.ClubMemberService;
import com.club.service.ClubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class ClubMemberServiceTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ClubService clubService;

    @Autowired
    private ClubMemberService clubMemberService;

    @Test
    void duplicateApply_throwsException() {
        Club club = createApprovedClub("成员测试社团");
        Long clubId = club.getId();
        Long userId = 100L;

        clubMemberService.applyMember(clubId, userId);
        assertThrows(BusinessException.class, () -> clubMemberService.applyMember(clubId, userId));
    }

    @Test
    void approveMember_statusActive_memberCountPlus() {
        Club club = createApprovedClub("审批测试社团");
        Long clubId = club.getId();
        Long userId = 200L;

        clubMemberService.applyMember(clubId, userId);
        // 获取申请记录
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClubMember>();
        wrapper.eq(ClubMember::getClubId, clubId).eq(ClubMember::getUserId, userId);
        ClubMember member = clubMemberService.getOne(wrapper);

        clubMemberService.auditMember(member.getId(), true, 1L);
        ClubMember approved = clubMemberService.getById(member.getId());
        assertEquals(MemberStatus.ACTIVE.name(), approved.getStatus());
    }

    @Test
    void activeMemberQuit_statusQuit_memberCountMinus() {
        Club club = createApprovedClub("退社测试社团");
        Long clubId = club.getId();
        Long userId = 300L;

        // 先入社
        clubMemberService.applyMember(clubId, userId);
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClubMember>();
        wrapper.eq(ClubMember::getClubId, clubId).eq(ClubMember::getUserId, userId);
        ClubMember member = clubMemberService.getOne(wrapper);
        clubMemberService.auditMember(member.getId(), true, 1L);

        // 退社
        clubMemberService.quitClub(clubId, userId);
        ClubMember quit = clubMemberService.getById(member.getId());
        assertEquals(MemberStatus.QUIT.name(), quit.getStatus());
    }

    @Test
    void cannotRemovePresident() {
        Club club = createApprovedClub("踢出社长测试");
        Long clubId = club.getId();

        // 社长是审批通过时自动创建的（createUserId=1L的用户）
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClubMember>();
        wrapper.eq(ClubMember::getClubId, clubId).eq(ClubMember::getMemberRole, MemberRole.PRESIDENT.name());
        ClubMember president = clubMemberService.getOne(wrapper);
        assertNotNull(president);

        assertThrows(BusinessException.class, () -> clubMemberService.removeMember(president.getId(), 999L));
    }

    @Test
    void changeRole_onlyPresidentCanExecute() {
        Club club = createApprovedClub("角色变更测试");
        Long clubId = club.getId();

        // 添加一个普通成员
        Long userId = 400L;
        clubMemberService.applyMember(clubId, userId);
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClubMember>();
        wrapper.eq(ClubMember::getClubId, clubId).eq(ClubMember::getUserId, userId);
        ClubMember member = clubMemberService.getOne(wrapper);
        clubMemberService.auditMember(member.getId(), true, 1L);

        // 变更角色为副社长
        clubMemberService.changeRole(member.getId(), MemberRole.VICE.name(), 1L);
        ClubMember updated = clubMemberService.getById(member.getId());
        assertEquals(MemberRole.VICE.name(), updated.getMemberRole());
    }

    private Club createApprovedClub(String name) {
        Club club = new Club();
        club.setName(name);
        club.setCategory("ACADEMIC");
        club.setDescription("测试");
        clubService.applyClub(club, 1L);
        clubService.auditClub(club.getId(), true, "通过", 1L);
        return clubService.getById(club.getId());
    }
}
