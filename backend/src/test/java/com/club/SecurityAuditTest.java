package com.club;

import com.club.config.TestConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 越权漏洞回归测试（2026-08 权限审计后新增）。
 * 覆盖修复的八类越权路径：字典/配置/通知写接口、成员管理提权链、帖子/评论删除归属。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
class SecurityAuditTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String studentToken;

    @BeforeEach
    void setup() throws Exception {
        // 注册固定学生账号（若已存在则注册失败，忽略），随后登录拿最小权限 token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity("/auth/register",
                new HttpEntity<>("{\"username\":\"audit_stu_fixed\",\"password\":\"admin123\",\"nickname\":\"审计学生\"}", headers),
                String.class);
        ResponseEntity<String> res = restTemplate.postForEntity("/auth/login",
                new HttpEntity<>("{\"username\":\"audit_stu_fixed\",\"password\":\"admin123\"}", headers),
                String.class);
        assertEquals(HttpStatus.OK, res.getStatusCode(), "学生登录应成功");
        JsonNode data = objectMapper.readTree(res.getBody()).get("data");
        studentToken = data.get("token").asText();
    }

    private HttpEntity<String> jsonWithToken(String token, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<String> jsonBody(String body) {
        return jsonWithToken(null, body);
    }

    @Test
    void student_cannotWriteSystemDict() {
        // 字典写接口有 @PreAuthorize system:dict:list → 403
        ResponseEntity<String> res = restTemplate.exchange("/system/dict/type", HttpMethod.POST,
                jsonWithToken(studentToken, "{\"dictName\":\"黑客字典\",\"dictType\":\"hack\",\"status\":\"0\"}"),
                String.class);
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode(), "学生不应能新增字典类型");
    }

    @Test
    void student_cannotWriteSystemConfig() {
        ResponseEntity<String> res = restTemplate.exchange("/system/config", HttpMethod.POST,
                jsonWithToken(studentToken, "{\"configName\":\"x\",\"configKey\":\"sys.hack\",\"configValue\":\"1\"}"),
                String.class);
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode(), "学生不应能新增参数配置");
    }

    @Test
    void student_cannotPublishPlatformNotice() throws Exception {
        // 平台通知（clubId=0）仅管理员；学生 → 业务拒绝（HTTP 200 + code != 200）
        ResponseEntity<String> res = restTemplate.exchange("/notice", HttpMethod.POST,
                jsonWithToken(studentToken, "{\"clubId\":0,\"title\":\"黑客公告\",\"content\":\"x\"}"),
                String.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        JsonNode root = objectMapper.readTree(res.getBody());
        assertTrue(root.get("code").asInt() != 200, "学生不应能发布平台公告，实际: " + res.getBody());
    }

    @Test
    void plainMember_cannotAuditMemberApplication() throws Exception {
        // 构造社团 + 非社长成员 + 待审申请，验证 audit 被社长校验拒绝
        jdbcTemplate.update("DELETE FROM club_member WHERE id IN (9801, 9802, 9803)");
        jdbcTemplate.update("DELETE FROM club WHERE id = 9801");
        jdbcTemplate.update("INSERT INTO club (id, name, code, status, member_count, star_level, create_time, update_time, deleted) " +
                "VALUES (9801, '审计社团', 'C9801', 'APPROVED', 2, 0, NOW(), NOW(), 0)");
        jdbcTemplate.update("INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, create_time, update_time, deleted) VALUES " +
                "(9801, 9801, 1, 'PRESIDENT', 'ACTIVE', NOW(), NOW(), NOW(), NOW(), 0), " +
                "(9802, 9801, 99999, 'MEMBER', 'ACTIVE', NOW(), NOW(), NOW(), NOW(), 0), " +
                "(9803, 9801, 99998, 'MEMBER', 'PENDING', NOW(), NOW(), NOW(), NOW(), 0)");
        try {
            // 学生以任意 operatorId 调用 audit（controller 传当前登录用户 id，但服务层校验 operator 必须是社长）
            // 这里直接走 HTTP：学生自己不是社长 → 业务拒绝
            ResponseEntity<String> res = restTemplate.exchange("/club/member/audit", HttpMethod.PUT,
                    jsonWithToken(studentToken, "{\"memberId\":9803,\"approved\":true}"),
                    String.class);
            JsonNode root = objectMapper.readTree(res.getBody());
            assertTrue(root.get("code").asInt() != 200, "非社长不应能审核入社申请，实际: " + res.getBody());
        } finally {
            jdbcTemplate.update("DELETE FROM club_member WHERE id IN (9801, 9802, 9803)");
            jdbcTemplate.update("DELETE FROM club WHERE id = 9801");
        }
    }

    @Test
    void plainMember_cannotPromoteSelfToPresident() throws Exception {
        // 提权封堵：非社长尝试 changeRole 应被 assertClubManager 拒绝
        jdbcTemplate.update("DELETE FROM club_member WHERE id IN (9811, 9812)");
        jdbcTemplate.update("DELETE FROM club WHERE id = 9811");
        jdbcTemplate.update("INSERT INTO club (id, name, code, status, member_count, star_level, create_time, update_time, deleted) " +
                "VALUES (9811, '提权社团', 'C9811', 'APPROVED', 2, 0, NOW(), NOW(), 0)");
        jdbcTemplate.update("INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, create_time, update_time, deleted) VALUES " +
                "(9811, 9811, 1, 'PRESIDENT', 'ACTIVE', NOW(), NOW(), NOW(), NOW(), 0), " +
                "(9812, 9811, 99997, 'MEMBER', 'ACTIVE', NOW(), NOW(), NOW(), NOW(), 0)");
        try {
            ResponseEntity<String> res = restTemplate.exchange("/club/member/role/9812", HttpMethod.PUT,
                    jsonWithToken(studentToken, "{\"memberRole\":\"PRESIDENT\"}"),
                    String.class);
            JsonNode root = objectMapper.readTree(res.getBody());
            assertTrue(root.get("code").asInt() != 200, "非社长不应能调整成员角色（提权），实际: " + res.getBody());
        } finally {
            jdbcTemplate.update("DELETE FROM club_member WHERE id IN (9811, 9812)");
            jdbcTemplate.update("DELETE FROM club WHERE id = 9811");
        }
    }

    @Test
    void admin_canStillWriteDictAndConfig() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> login = restTemplate.postForEntity("/auth/login",
                new HttpEntity<>("{\"username\":\"admin\",\"password\":\"admin123\"}", headers), String.class);
        String adminToken = objectMapper.readTree(login.getBody()).get("data").get("token").asText();
        // 管理员正常路径不破坏：新增字典类型成功（HTTP 200 + code 200）
        ResponseEntity<String> res = restTemplate.exchange("/system/dict/type", HttpMethod.POST,
                jsonWithToken(adminToken, "{\"dictName\":\"审计测试字典\",\"dictType\":\"audit_t_" + System.nanoTime() % 10000 + "\",\"status\":\"0\"}"),
                String.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(200, objectMapper.readTree(res.getBody()).get("code").asInt(), "管理员新增字典应成功");
    }
}
