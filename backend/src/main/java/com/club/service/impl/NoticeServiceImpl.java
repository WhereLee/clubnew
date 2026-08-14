package com.club.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.domain.Notice;
import com.club.mapper.NoticeMapper;
import com.club.service.NoticeService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    private static final String NOTICE_CACHE_PREFIX = "notice:";
    private static final String NULL_CACHE = "NULL";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public IPage<Notice> listPage(Integer pageNum, Integer pageSize, String title) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) wrapper.like(Notice::getTitle, title);
        wrapper.eq(Notice::getStatus, "0");
        wrapper.orderByDesc(Notice::getTop, Notice::getPublishTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Long publishNotice(Notice notice) {
        notice.setPublishTime(LocalDateTime.now());
        notice.setStatus("0");
        notice.setTop("N");
        save(notice);
        return notice.getId();
    }

    @Override
    public Notice getById(java.io.Serializable id) {
        String cacheKey = NOTICE_CACHE_PREFIX + id;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            if (NULL_CACHE.equals(cached)) return null;
            return cn.hutool.json.JSONUtil.toBean(cached, Notice.class);
        }
        // 缓存击穿防护：简单互斥（用 SETNX）
        String lockKey = "lock:notice:" + id;
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 5, TimeUnit.SECONDS);
        try {
            Notice notice = super.getById(id);
            long ttl = 60 + RandomUtil.randomInt(0, 30);
            if (notice != null) {
                stringRedisTemplate.opsForValue().set(cacheKey, cn.hutool.json.JSONUtil.toJsonStr(notice), ttl, TimeUnit.SECONDS);
            } else {
                // 缓存穿透防护：写入空值
                stringRedisTemplate.opsForValue().set(cacheKey, NULL_CACHE, 60, TimeUnit.SECONDS);
            }
            return notice;
        } finally {
            if (Boolean.TRUE.equals(locked)) {
                stringRedisTemplate.delete(lockKey);
            }
        }
    }

    public void clearCache(Long id) {
        stringRedisTemplate.delete(NOTICE_CACHE_PREFIX + id);
    }

    @Override
    public boolean updateById(Notice entity) {
        boolean result = super.updateById(entity);
        if (result) clearCache(entity.getId());
        return result;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean result = super.removeById(id);
        if (result) clearCache(Long.parseLong(id.toString()));
        return result;
    }
}
