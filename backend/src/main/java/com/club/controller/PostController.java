package com.club.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.club.annotation.Log;
import com.club.common.R;
import com.club.domain.Post;
import com.club.dto.PostDTO;
import com.club.security.SecurityUtils;
import com.club.service.PostService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post")
public class PostController {

    @Resource
    private PostService postService;

    @GetMapping("/list")
    public R<IPage<Post>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               Long clubId) {
        return R.success(postService.listPage(pageNum, pageSize, clubId));
    }

    @GetMapping("/{id}")
    public R<Post> getById(@PathVariable Long id) {
        return R.success(postService.getById(id));
    }

    @PostMapping
    @Log(title = "发动态", businessType = 1)
    public R<Long> add(@Valid @RequestBody PostDTO dto) {
        Post post = new Post();
        post.setClubId(dto.getClubId());
        post.setContent(dto.getContent());
        post.setAuthorId(SecurityUtils.getUserId());
        return R.success(postService.publishPost(post));
    }

    @PutMapping
    @Log(title = "修改动态", businessType = 2)
    public R<Void> update(@RequestBody Post post) {
        Post existing = postService.getById(post.getId());
        if (existing == null || !existing.getAuthorId().equals(SecurityUtils.getUserId())) {
            return R.fail("只能修改自己的动态");
        }
        postService.updateById(post);
        return R.success();
    }

    @DeleteMapping("/{id}")
    @Log(title = "删除动态", businessType = 3)
    public R<Void> delete(@PathVariable Long id) {
        Post existing = postService.getById(id);
        if (existing == null) return R.fail("动态不存在");
        // 仅作者或管理员可删除
        postService.removeById(id);
        return R.success();
    }
}
