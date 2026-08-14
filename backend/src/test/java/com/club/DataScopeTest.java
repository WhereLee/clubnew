package com.club;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.club.annotation.DataScope;
import com.club.aspect.DataScopeAspect;
import com.club.config.TestConfig;
import com.club.domain.Club;
import com.club.security.LoginUser;
import com.club.service.ClubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Annotation;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据权限真实效果测试：验证 @DataScope 切面生成的过滤片段被 ClubServiceImpl.listPage 真实消费，
 * 不同 data_scope 角色看到不同范围的数据。
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class DataScopeTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private DataScopeAspect dataScopeAspect;

    @Autowired
    private ClubService clubService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 手写 DataScope 注解实例，用于手动触发切面（等价于 Controller 方法上的 @DataScope） */
    private DataScope mockDataScope() {
        return new DataScope() {
            @Override public String deptAlias() { return "club"; }
            @Override public String userAlias() { return "club"; }
            @Override public Class<? extends Annotation> annotationType() { return DataScope.class; }
        };
    }

    private void loginAs(Long userId, String userType) {
        LoginUser user = new LoginUser(userId, "u" + userId, "", "测试用户", userType, Set.of("*:*:*"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    private void insertClub(long id, String name, long createUserId) {
        jdbcTemplate.update(
                "INSERT INTO club (id, name, code, description, category, status, member_count, star_level, create_user_id, create_time, update_time, deleted) " +
                "VALUES (?,?,?,?,?,?,?,?,?,NOW(),NOW(),0)",
                id, name, "CODE" + id, "测试社团", "ACADEMIC", "APPROVED", 0, 0, createUserId);
    }

    @Test
    void adminSeesAllClubs() {
        // 清空其他测试残留的 club 数据，保证断言用绝对数量可靠
        jdbcTemplate.update("DELETE FROM club");
        // 造一个学生用户 + 学生角色关联（data_scope=4 仅本人）
        jdbcTemplate.update("INSERT INTO sys_user (id, username, nickname, user_type, status, create_time, update_time, deleted) VALUES (100, 'stu100', '学生', 'STUDENT', '0', NOW(), NOW(), 0)");
        jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (100, 3)");
        insertClub(1001, "管理员社团", 1L);
        insertClub(1002, "学生社团A", 100L);

        // 管理员（ADMIN）不应加过滤，看到全部
        loginAs(1L, "ADMIN");
        dataScopeAspect.doBefore(mockDataScope());
        try {
            IPage<Club> page = clubService.listPage(1, 100, null, null, null);
            assertEquals(2, page.getTotal(), "管理员应看到全部社团");
        } finally {
            dataScopeAspect.doAfter();
        }
        SecurityContextHolder.clearContext();
    }

    @Test
    void studentSeesOnlyOwnClubs() {
        // 清空其他测试残留的 club 数据，保证断言用绝对数量可靠
        jdbcTemplate.update("DELETE FROM club");
        // 学生用户 + 学生角色（data_scope=4 仅本人）
        jdbcTemplate.update("INSERT INTO sys_user (id, username, nickname, user_type, status, create_time, update_time, deleted) VALUES (200, 'stu200', '学生', 'STUDENT', '0', NOW(), NOW(), 0)");
        jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (200, 3)");
        insertClub(2001, "别人社团", 1L);
        insertClub(2002, "我的社团A", 200L);
        insertClub(2003, "我的社团B", 200L);

        // 学生（data_scope=4）只应看到自己创建的社团
        loginAs(200L, "STUDENT");
        dataScopeAspect.doBefore(mockDataScope());
        try {
            IPage<Club> page = clubService.listPage(1, 100, null, null, null);
            assertEquals(2, page.getTotal(), "学生应只看到自己创建的社团");
            for (Club club : page.getRecords()) {
                assertEquals(200L, club.getCreateUserId(), "过滤后的社团创建人应为学生本人");
            }
        } finally {
            dataScopeAspect.doAfter();
        }
        SecurityContextHolder.clearContext();
    }
}
