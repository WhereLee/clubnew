package com.club.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.domain.SysDictData;
import com.club.mapper.SysDictDataMapper;
import com.club.service.SysDictDataService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    private static final Logger log = LoggerFactory.getLogger(SysDictDataServiceImpl.class);

    private static final String DICT_CACHE_PREFIX = "sys_dict:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public IPage<SysDictData> listPage(Integer pageNum, Integer pageSize, String dictType, String dictLabel) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dictType)) {
            wrapper.eq(SysDictData::getDictType, dictType);
        }
        if (StringUtils.hasText(dictLabel)) {
            wrapper.like(SysDictData::getDictLabel, dictLabel);
        }
        wrapper.orderByAsc(SysDictData::getDictSort);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 缓存职责收口在 Service 层：任何写操作后自动清除对应类型的缓存，
     * Controller 不感知缓存实现（Cache-Aside 一致性由 Service 保证）。
     */
    @Override
    public boolean save(SysDictData entity) {
        boolean ok = super.save(entity);
        if (ok) evictCache(entity.getDictType());
        return ok;
    }

    @Override
    public boolean updateById(SysDictData entity) {
        // 编辑时前端可能不传 dictType，先查旧记录保证缓存键正确
        SysDictData old = getById(entity.getId());
        boolean ok = super.updateById(entity);
        if (ok) {
            evictCache(old != null ? old.getDictType() : entity.getDictType());
        }
        return ok;
    }

    @Override
    public boolean removeById(Serializable id) {
        SysDictData old = getById(id);
        boolean ok = super.removeById(id);
        if (ok && old != null) evictCache(old.getDictType());
        return ok;
    }

    /** 清除字典缓存（Redis 不可用时忽略，缓存由 TTL 自然过期） */
    private void evictCache(String dictType) {
        if (!StringUtils.hasText(dictType)) return;
        try {
            stringRedisTemplate.delete(DICT_CACHE_PREFIX + dictType);
        } catch (Exception e) {
            log.warn("字典缓存清除失败: {}", e.getMessage());
        }
    }

    @Override
    public List<SysDictData> getByType(String dictType) {
        String cacheKey = DICT_CACHE_PREFIX + dictType;
        // 1. 先查缓存
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return JSONUtil.toList(cached, SysDictData.class);
        }
        // 2. 缓存未命中，查库
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictType, dictType)
               .eq(SysDictData::getStatus, "0")
               .orderByAsc(SysDictData::getDictSort);
        List<SysDictData> list = list(wrapper);
        // 3. 写入缓存（带随机过期防雪崩）
        if (list != null && !list.isEmpty()) {
            long ttl = 60 + RandomUtil.randomInt(0, 30);
            stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(list), ttl, TimeUnit.SECONDS);
        }
        return list;
    }
}
