package com.club;

import com.club.config.TestConfig;
import com.club.domain.SysDictData;
import com.club.service.SysDictDataService;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class DictDataServiceImplTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SysDictDataService dictDataService;

    private ValueOperations<String, String> valueOps;

    @BeforeEach
    void setup() {
        valueOps = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void getByType_cacheMiss_thenQueryDbAndSetCache() {
        // 缓存未命中
        when(valueOps.get("sys_dict:club_status")).thenReturn(null);

        List<SysDictData> result = dictDataService.getByType("club_status");

        // 验证先查了Redis
        verify(valueOps).get("sys_dict:club_status");
        // 验证查库后写回Redis
        verify(valueOps).set(eq("sys_dict:club_status"), anyString(), anyLong(), eq(TimeUnit.SECONDS));
        // 验证返回了数据
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void afterSave_thenCacheEvicted() {
        // 新增一条字典数据
        SysDictData data = new SysDictData();
        data.setDictSort(99);
        data.setDictLabel("test_label");
        data.setDictValue("test");
        data.setDictType("club_status");
        data.setIsDefault("N");
        data.setStatus("0");
        dictDataService.save(data);

        // 验证缓存被删除
        verify(stringRedisTemplate).delete("sys_dict:club_status");
    }
}
