package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.club.common.BusinessException;
import com.club.domain.Comment;
import com.club.domain.Post;
import com.club.domain.UserLike;
import com.club.mapper.CommentMapper;
import com.club.mapper.PostMapper;
import com.club.mapper.UserLikeMapper;
import com.club.event.ClubEventPublisher;
import com.club.event.EventType;
import com.club.service.LikeService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeServiceImpl implements LikeService {

    @Resource
    private UserLikeMapper userLikeMapper;

    @Resource
    private PostMapper postMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private ClubEventPublisher eventPublisher;

    @Override
    @Transactional
    public boolean toggleLike(String bizType, Long bizId, Long userId) {
        LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLike::getBizType, bizType).eq(UserLike::getBizId, bizId).eq(UserLike::getUserId, userId);
        UserLike existing = userLikeMapper.selectOne(wrapper);
        if (existing != null && "1".equals(existing.getStatus())) {
            // 取消点赞
            existing.setStatus("0");
            userLikeMapper.updateById(existing);
            updateLikeCount(bizType, bizId, -1);
            return false;
        } else if (existing != null && "0".equals(existing.getStatus())) {
            // 重新点赞
            existing.setStatus("1");
            userLikeMapper.updateById(existing);
            updateLikeCount(bizType, bizId, 1);
            return true;
        } else {
            // 新增点赞
            UserLike like = new UserLike();
            like.setBizType(bizType);
            like.setBizId(bizId);
            like.setUserId(userId);
            like.setStatus("1");
            userLikeMapper.insert(like);
            updateLikeCount(bizType, bizId, 1);
            return true;
        }
    }

    @Override
    public boolean isLiked(String bizType, Long bizId, Long userId) {
        LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLike::getBizType, bizType).eq(UserLike::getBizId, bizId)
               .eq(UserLike::getUserId, userId).eq(UserLike::getStatus, "1");
        return userLikeMapper.selectCount(wrapper) > 0;
    }

    private void updateLikeCount(String bizType, Long bizId, int delta) {
        if ("POST".equals(bizType)) {
            jdbcTemplate.update("UPDATE post SET like_count = GREATEST(like_count + ?, 0) WHERE id = ?", delta, bizId);
            Post post = postMapper.selectById(bizId);
            if (post != null && post.getClubId() != null) {
                // 点赞事件：消费方同时提升动态热度与社团活跃度
                eventPublisher.publish(EventType.LIKED, post.getClubId(), null, bizId, "POST");
            }
        } else if ("COMMENT".equals(bizType)) {
            jdbcTemplate.update("UPDATE comment SET like_count = GREATEST(like_count + ?, 0) WHERE id = ?", delta, bizId);
        }
    }
}
