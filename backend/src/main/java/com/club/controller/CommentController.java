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
        commentService.removeById(id);
        return R.success();
    }
}
