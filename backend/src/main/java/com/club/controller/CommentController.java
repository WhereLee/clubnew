package com.club.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.club.annotation.Log;
import com.club.common.R;
import com.club.domain.Comment;
import com.club.dto.CommentDTO;
import com.club.security.SecurityUtils;
import com.club.service.CommentService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @GetMapping("/list")
    public R<IPage<Comment>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  String bizType, Long bizId) {
        return R.success(commentService.listPage(pageNum, pageSize, bizType, bizId));
    }

    @PostMapping
    @Log(title = "发评论", businessType = 1)
    public R<Long> add(@Valid @RequestBody CommentDTO dto) {
        Comment comment = new Comment();
        comment.setBizType(dto.getBizType());
        comment.setBizId(dto.getBizId());
        comment.setContent(dto.getContent());
        comment.setUserId(SecurityUtils.getUserId());
        return R.success(commentService.addComment(comment));
    }

    @DeleteMapping("/{id}")
    @Log(title = "删评论", businessType = 3)
    public R<Void> delete(@PathVariable Long id) {
        Comment existing = commentService.getById(id);
        if (existing == null) {
            return R.fail("评论不存在");
        }
        // 仅作者或管理员可删除（防任意删除他人评论）
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.getLoginUser() != null
                && "ADMIN".equals(SecurityUtils.getLoginUser().getUserType());
        if (!isAdmin && !existing.getUserId().equals(userId)) {
            return R.fail("只能删除自己的评论");
        }
        commentService.removeById(id);
        return R.success();
    }
}
