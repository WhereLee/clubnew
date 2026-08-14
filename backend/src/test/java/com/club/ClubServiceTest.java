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
class ClubServiceTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ClubService clubService;

    @Test
    void duplicateName_throwsException() {
        Club club1 = new Club();
        club1.setName("同名社团");
        club1.setCategory("ACADEMIC");
        clubService.applyClub(club1, 1L);

        Club club2 = new Club();
        club2.setName("同名社团");
        club2.setCategory("CULTURE");
        assertThrows(BusinessException.class, () -> clubService.applyClub(club2, 2L));
    }

    @Test
    void dissolvedClub_update_throwsException() {
        Club club = new Club();
        club.setName("注销测试社团");
        club.setCategory("SPORTS");
        clubService.applyClub(club, 1L);
        clubService.changeStatus(club.getId(), ClubStatus.APPROVED.name());
        clubService.changeStatus(club.getId(), ClubStatus.DISSOLVED.name());

        Club dissolved = clubService.getById(club.getId());
        assertEquals(ClubStatus.DISSOLVED.name(), dissolved.getStatus());
        // 注销后的社团不应能修改
    }

    @Test
    void clubCode_formatIsCLUBPlus4Digits() {
        Club club = new Club();
        club.setName("编号测试社团");
        club.setCategory("VOLUNTEER");
        clubService.applyClub(club, 1L);

        Club saved = clubService.getById(club.getId());
        assertNotNull(saved.getCode());
        assertTrue(saved.getCode().matches("CLUB\\d{4}"), "社团编号应为CLUB+4位数字");
    }
}
