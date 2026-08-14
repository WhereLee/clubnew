package com.club.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.domain.Post;
import com.club.mapper.PostMapper;
import com.club.event.ClubEventPublisher;
import com.club.event.EventType;
import com.club.service.PostService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private static final String POST_CACHE_PREFIX = "post:";
    private static final String NULL_CACHE = "NULL";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ClubEventPublisher eventPublisher;

    @Override
    public IPage<Post> listPage(Integer pageNum, Integer pageSize, Long clubId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (clubId != null) wrapper.eq(Post::getClubId, clubId);
        wrapper.eq(Post::getStatus, "0");
        wrapper.orderByDesc(Post::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Long publishPost(Post post) {
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus("0");
        save(post);
        // 发布互动事件（Redis Stream 异步消费加分，失败降级为同步加分）
        eventPublisher.publish(EventType.POST_CREATED, post.getClubId(), post.getAuthorId(), post.getId(), null);
        return post.getId();
    }

    @Override
    public Post getById(java.io.Serializable id) {
        String cacheKey = POST_CACHE_PREFIX + id;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            if (NULL_CACHE.equals(cached)) return null;
            return cn.hutool.json.JSONUtil.toBean(cached, Post.class);
        }
        Post post = super.getById(id);
        long ttl = 60 + RandomUtil.randomInt(0, 30);
        if (post != null) {
            stringRedisTemplate.opsForValue().set(cacheKey, cn.hutool.json.JSONUtil.toJsonStr(post), ttl, TimeUnit.SECONDS);
        } else {
            stringRedisTemplate.opsForValue().set(cacheKey, NULL_CACHE, 60, TimeUnit.SECONDS);
        }
        return post;
    }

    public void clearCache(Long id) {
        stringRedisTemplate.delete(POST_CACHE_PREFIX + id);
    }

    @Override
    public boolean updateById(Post entity) {
        boolean result = super.updateById(entity);
        if (result) clearCache(entity.getId());
        return result;
    }
}
