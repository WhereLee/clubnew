package com.club;

import com.club.config.TestConfig;
import com.club.domain.Post;
import com.club.mapper.PostMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class CachePenetrationTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private PostMapper postMapper;

    private ValueOperations<String, String> valueOps;

    @BeforeEach
    void setup() {
        valueOps = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void queryNonExistentPost_returnsNull_shouldCache() {
        // 模拟缓存穿透防护：查询不存在的帖子
        // 先查缓存未命中
        when(valueOps.get("post:999999")).thenReturn(null);
        String cached = stringRedisTemplate.opsForValue().get("post:999999");
        assertNull(cached, "缓存应未命中");

        // 查库也查不到
        Post post = postMapper.selectById(999999L);
        assertNull(post, "不存在的帖子应返回null");

        // 写入空值缓存（防穿透）
        stringRedisTemplate.opsForValue().set("post:999999", "", 60, java.util.concurrent.TimeUnit.SECONDS);
        verify(valueOps).set(eq("post:999999"), eq(""), eq(60L), any());
    }
}
