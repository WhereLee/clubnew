package com.club.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.club.annotation.Log;
import com.club.common.R;
import com.club.domain.Notice;
import com.club.dto.NoticeDTO;
import com.club.security.SecurityUtils;
import com.club.service.NoticeService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    @GetMapping("/list")
    public R<IPage<Notice>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 String title) {
        return R.success(noticeService.listPage(pageNum, pageSize, title));
    }

    @GetMapping("/{id}")
    public R<Notice> getById(@PathVariable Long id) {
        return R.success(noticeService.getById(id));
    }

    @PostMapping
    @Log(title = "公告管理", businessType = 1)
    public R<Long> add(@Valid @RequestBody NoticeDTO dto) {
        Notice notice = new Notice();
        notice.setClubId(dto.getClubId() != null ? dto.getClubId() : 0L);
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setPublishUserId(SecurityUtils.getUserId());
        return R.success(noticeService.publishNotice(notice));
    }

    @PutMapping
    @Log(title = "公告管理", businessType = 2)
    public R<Void> update(@RequestBody Notice notice) {
        noticeService.updateById(notice);
        return R.success();
    }

    @DeleteMapping("/{ids}")
    @Log(title = "公告管理", businessType = 3)
    public R<Void> delete(@PathVariable String ids) {
        for (String id : ids.split(",")) {
            noticeService.removeById(Long.parseLong(id));
        }
        return R.success();
    }

    @PutMapping("/{id}/top")
    @Log(title = "公告置顶", businessType = 2)
    public R<Void> top(@PathVariable Long id, @RequestBody java.util.Map<String, String> params) {
        Notice notice = noticeService.getById(id);
        notice.setTop(params.getOrDefault("top", "N"));
        noticeService.updateById(notice);
        return R.success();
    }
}
