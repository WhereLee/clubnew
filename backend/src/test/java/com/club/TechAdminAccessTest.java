package com.club;

import com.club.config.TestConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 技术管理员权限边界测试（V4：职责分离）。
 *
 * tech_admin 能看运行数据（监控概览/操作日志/登录日志），
 * 但访问业务管理接口必须 403——"能看不能碰"。
 * 使用 RANDOM_PORT + TestRestTemplate 走完整 Tomcat 链路（含 context-path /api），
 * 与生产环境行为一致。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
class TechAdminAccessTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /** 登录并返回 access token（种子账号：admin/admin123、tech_admin/admin123 由 V1/V4 提供） */
    private String login(String username, String password) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> res = restTemplate.postForEntity(
                "/auth/login",
                new HttpEntity<>("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}", headers),
                String.class);
        assertEquals(HttpStatus.OK, res.getStatusCode(), "登录应 HTTP 200");
        JsonNode root = objectMapper.readTree(res.getBody());
        JsonNode data = root.get("data");
        if (data == null || data.isNull()) {
            throw new AssertionError("登录业务失败，完整响应: " + res.getBody());
        }
        assertTrue(data.has("token"), "登录应返回 token，实际 data: " + data);
        assertTrue(data.has("userType"), "登录应返回 userType");
        return data.get("token").asText();
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void techAdmin_canSeeMonitorOverview() throws Exception {
        String token = login("tech_admin", "admin123");
        ResponseEntity<String> res = restTemplate.exchange(
                "/monitor/overview", HttpMethod.GET,
                new HttpEntity<>(bearer(token)), String.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        JsonNode data = objectMapper.readTree(res.getBody()).get("data");
        assertNotNull(data, "概览应返回 data");
        assertTrue(data.has("jvm") && data.has("http") && data.has("business"), "概览应含 jvm/http/business 三块");
        assertTrue(data.get("jvm").has("heapUsedMb"), "JVM 指标应含堆内存");
        assertTrue(data.get("business").isArray() && data.get("business").size() >= 15, "业务计数器应齐全");
    }

    @Test
    void techAdmin_canSeeLogs() throws Exception {
        String token = login("tech_admin", "admin123");
        ResponseEntity<String> res1 = restTemplate.exchange(
                "/system/operlog/list", HttpMethod.GET,
                new HttpEntity<>(bearer(token)), String.class);
        assertEquals(HttpStatus.OK, res1.getStatusCode(), "tech_admin 应能查看操作日志");
        ResponseEntity<String> res2 = restTemplate.exchange(
                "/system/loginlog/list", HttpMethod.GET,
                new HttpEntity<>(bearer(token)), String.class);
        assertEquals(HttpStatus.OK, res2.getStatusCode(), "tech_admin 应能查看登录日志");
    }

    @Test
    void techAdmin_cannotTouchBusinessInterfaces() throws Exception {
        String token = login("tech_admin", "admin123");
        // 系统管理类接口（method security 负例）：技术管理员无业务权限，必须 403
        // 注：/club/list 为全员可见接口（社团广场），不在此列
        assertEquals(HttpStatus.FORBIDDEN,
                restTemplate.exchange("/system/user/list", HttpMethod.GET,
                        new HttpEntity<>(bearer(token)), String.class).getStatusCode(),
                "tech_admin 不应访问用户管理");
        assertEquals(HttpStatus.FORBIDDEN,
                restTemplate.exchange("/system/menu/list", HttpMethod.GET,
                        new HttpEntity<>(bearer(token)), String.class).getStatusCode(),
                "tech_admin 不应访问菜单管理");
        assertEquals(HttpStatus.FORBIDDEN,
                restTemplate.exchange("/system/role/list", HttpMethod.GET,
                        new HttpEntity<>(bearer(token)), String.class).getStatusCode(),
                "tech_admin 不应访问角色管理");
    }

    @Test
    void admin_keepsFullAccess_includingMonitor() throws Exception {
        String token = login("admin", "admin123");
        assertEquals(HttpStatus.OK,
                restTemplate.exchange("/monitor/overview", HttpMethod.GET,
                        new HttpEntity<>(bearer(token)), String.class).getStatusCode(),
                "admin 应能访问监控概览");
        assertEquals(HttpStatus.OK,
                restTemplate.exchange("/system/user/list", HttpMethod.GET,
                        new HttpEntity<>(bearer(token)), String.class).getStatusCode(),
                "admin 应保持用户管理权限");
    }

    @Test
    void anonymous_cannotSeeMonitor() {
        ResponseEntity<String> res = restTemplate.exchange("/monitor/overview", HttpMethod.GET,
                HttpEntity.EMPTY, String.class);
        System.out.println("[DIAG] anonymous monitor -> HTTP " + res.getStatusCode() + " body: " + res.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode(), "匿名访问监控概览应 401");
    }
}
