package com.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.Post;

public interface PostService extends IService<Post> {
    IPage<Post> listPage(Integer pageNum, Integer pageSize, Long clubId);
    Long publishPost(Post post);
}
