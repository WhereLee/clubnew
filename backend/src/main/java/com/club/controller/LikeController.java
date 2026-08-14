package com.club.controller;

import com.club.annotation.Log;
import com.club.common.R;
import com.club.dto.LikeDTO;
import com.club.security.SecurityUtils;
import com.club.service.LikeService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/like")
public class LikeController {

    @Resource
    private LikeService likeService;

    @PostMapping
    @Log(title = "点赞", businessType = 1)
    public R<Boolean> like(@Valid @RequestBody LikeDTO dto) {
        Long userId = SecurityUtils.getUserId();
        boolean liked = likeService.toggleLike(dto.getBizType(), dto.getBizId(), userId);
        return R.success(liked);
    }

    @DeleteMapping
    @Log(title = "取消点赞", businessType = 3)
    public R<Void> unlike(@Valid @RequestBody LikeDTO dto) {
        Long userId = SecurityUtils.getUserId();
        likeService.toggleLike(dto.getBizType(), dto.getBizId(), userId);
        return R.success();
    }
}
