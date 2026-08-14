package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.domain.Comment;
import com.club.domain.Post;
import com.club.mapper.CommentMapper;
import com.club.mapper.PostMapper;
import com.club.event.ClubEventPublisher;
import com.club.event.EventType;
import com.club.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private PostMapper postMapper;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private ClubEventPublisher eventPublisher;

    @Override
    public IPage<Comment> listPage(Integer pageNum, Integer pageSize, String bizType, Long bizId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getBizType, bizType).eq(Comment::getBizId, bizId);
        wrapper.orderByDesc(Comment::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional
    public Long addComment(Comment comment) {
        comment.setLikeCount(0);
        save(comment);
        // 更新评论计数
        if ("POST".equals(comment.getBizType())) {
            jdbcTemplate.update("UPDATE post SET comment_count = comment_count + 1 WHERE id = ?", comment.getBizId());
        }
        // 发布评论事件（Stream 异步消费加分；clubId 同步查出，走 post 缓存，成本可忽略）
        Long clubId = null;
        if ("POST".equals(comment.getBizType())) {
            Post post = postMapper.selectById(comment.getBizId());
            if (post != null) clubId = post.getClubId();
        }
        if (clubId != null) {
            eventPublisher.publish(EventType.COMMENT_CREATED, clubId, comment.getUserId(), comment.getId(), comment.getBizType());
        }
        return comment.getId();
    }
}