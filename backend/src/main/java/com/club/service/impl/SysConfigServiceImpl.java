package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.domain.SysConfig;
import com.club.mapper.SysConfigMapper;
import com.club.service.SysConfigService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    private static final Logger log = LoggerFactory.getLogger(SysConfigServiceImpl.class);

    private static final String CONFIG_CACHE_PREFIX = "sys_config:";
    private static final String NULL_MARKER = "__NULL__";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public IPage<SysConfig> listPage(Integer pageNum, Integer pageSize, String configName, String configKey) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(configName)) {
            wrapper.like(SysConfig::getConfigName, configName);
        }
        if (StringUtils.hasText(configKey)) {
            wrapper.like(SysConfig::getConfigKey, configKey);
        }
        wrapper.orderByAsc(SysConfig::getId);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public String getConfigValue(String configKey) {
        String cacheKey = CONFIG_CACHE_PREFIX + configKey;
        // 1. 先查缓存
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            if (NULL_MARKER.equals(cached)) return null;
            return cached;
        }
        // 2. 缓存未命中，查库
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);
        SysConfig config = getOne(wrapper);
        long ttl = 60 + (long) (Math.random() * 30);
        if (config != null) {
            // 3. 写入缓存
            stringRedisTemplate.opsForValue().set(cacheKey, config.getConfigValue(), ttl, TimeUnit.SECONDS);
            return config.getConfigValue();
        }
        // 缓存穿透防护：写入空值标记
        stringRedisTemplate.opsForValue().set(cacheKey, NULL_MARKER, 60, TimeUnit.SECONDS);
        return null;
    }

    @Override
    public boolean save(SysConfig entity) {
        boolean result = super.save(entity);
        if (result) evictCache(entity.getConfigKey());
        return result;
    }

    @Override
    public boolean updateById(SysConfig entity) {
        // 编辑时前端可能不传 configKey，先查旧记录保证缓存键正确
        SysConfig old = getById(entity.getId());
        boolean result = super.updateById(entity);
        if (result) evictCache(old != null ? old.getConfigKey() : entity.getConfigKey());
        return result;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        SysConfig config = getById(id);
        boolean result = super.removeById(id);
        if (result && config != null) evictCache(config.getConfigKey());
        return result;
    }

    /** 清除参数缓存（Redis 不可用时忽略，缓存由 TTL 自然过期） */
    private void evictCache(String configKey) {
        if (!StringUtils.hasText(configKey)) return;
        try {
            stringRedisTemplate.delete(CONFIG_CACHE_PREFIX + configKey);
        } catch (Exception e) {
            log.warn("参数缓存清除失败: {}", e.getMessage());
        }
    }
}
