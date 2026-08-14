package com.club;

import com.club.common.BusinessException;
import com.club.config.TestConfig;
import com.club.domain.Club;
import com.club.enums.ClubStatus;
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
class ClubStatusMachineTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ClubService clubService;

    @Test
    void pendingToApproved_success() {
        Club club = createTestClub("测试社团A");
        assertEquals(ClubStatus.PENDING.name(), club.getStatus());
        clubService.changeStatus(club.getId(), ClubStatus.APPROVED.name());
        Club updated = clubService.getById(club.getId());
        assertEquals(ClubStatus.APPROVED.name(), updated.getStatus());
    }

    @Test
    void approvedToSuspendedToApprovedToDissolved_success() {
        Club club = createTestClub("测试社团B");
        clubService.changeStatus(club.getId(), ClubStatus.APPROVED.name());
        clubService.changeStatus(club.getId(), ClubStatus.SUSPENDED.name());
        clubService.changeStatus(club.getId(), ClubStatus.APPROVED.name());
        clubService.changeStatus(club.getId(), ClubStatus.DISSOLVED.name());
        Club updated = clubService.getById(club.getId());
        assertEquals(ClubStatus.DISSOLVED.name(), updated.getStatus());
    }

    @Test
    void approvedToPending_throwsException() {
        Club club = createTestClub("测试社团C");
        clubService.changeStatus(club.getId(), ClubStatus.APPROVED.name());
        assertThrows(BusinessException.class,
                () -> clubService.changeStatus(club.getId(), ClubStatus.PENDING.name()));
    }

    @Test
    void dissolved_cannotChangeStatus() {
        Club club = createTestClub("测试社团D");
        clubService.changeStatus(club.getId(), ClubStatus.APPROVED.name());
        clubService.changeStatus(club.getId(), ClubStatus.DISSOLVED.name());
        assertThrows(BusinessException.class,
                () -> clubService.changeStatus(club.getId(), ClubStatus.APPROVED.name()));
        assertThrows(BusinessException.class,
                () -> clubService.changeStatus(club.getId(), ClubStatus.SUSPENDED.name()));
        assertThrows(BusinessException.class,
                () -> clubService.changeStatus(club.getId(), ClubStatus.DISSOLVED.name()));
    }

    @Test
    void rejected_cannotChangeToApproved() {
        Club club = createTestClub("测试社团E");
        clubService.changeStatus(club.getId(), ClubStatus.REJECTED.name());
        assertThrows(BusinessException.class,
                () -> clubService.changeStatus(club.getId(), ClubStatus.APPROVED.name()));
    }

    private Club createTestClub(String name) {
        Club club = new Club();
        club.setName(name);
        club.setCategory("ACADEMIC");
        club.setDescription("测试社团");
        clubService.applyClub(club, 1L);
        return club;
    }
}
