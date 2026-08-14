package com.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.Comment;

public interface CommentService extends IService<Comment> {
    IPage<Comment> listPage(Integer pageNum, Integer pageSize, String bizType, Long bizId);
    Long addComment(Comment comment);
}
