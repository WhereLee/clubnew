package com.club.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.club.annotation.Log;
import com.club.common.BusinessException;
import com.club.common.R;
import com.club.domain.ClubMember;
import com.club.domain.Notice;
import com.club.dto.NoticeDTO;
import com.club.security.SecurityUtils;
import com.club.service.ClubMemberService;
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

    @Resource
    private ClubMemberService clubMemberService;

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
        Long clubId = dto.getClubId() != null ? dto.getClubId() : 0L;
        // 权限：平台通知（clubId=0）仅管理员；社团通知仅该社团社长或管理员
        checkPublishPerm(clubId);
        Notice notice = new Notice();
        notice.setClubId(clubId);
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setPublishUserId(SecurityUtils.getUserId());
        return R.success(noticeService.publishNotice(notice));
    }

    @PutMapping
    @Log(title = "公告管理", businessType = 2)
    public R<Void> update(@RequestBody Notice notice) {
        Notice existing = noticeService.getById(notice.getId());
        if (existing == null) {
            throw new BusinessException("公告不存在");
        }
        checkPublishPerm(existing.getClubId());
        // 仅允许修改标题/内容，防止越权篡改 clubId/发布人等归属字段
        notice.setClubId(existing.getClubId());
        notice.setPublishUserId(existing.getPublishUserId());
        noticeService.updateById(notice);
        return R.success();
    }

    @DeleteMapping("/{ids}")
    @Log(title = "公告管理", businessType = 3)
    public R<Void> delete(@PathVariable String ids) {
        for (String id : ids.split(",")) {
            Notice existing = noticeService.getById(Long.parseLong(id));
            if (existing != null) {
                checkPublishPerm(existing.getClubId());
                noticeService.removeById(existing.getId());
            }
        }
        return R.success();
    }

    @PutMapping("/{id}/top")
    @Log(title = "公告置顶", businessType = 2)
    public R<Void> top(@PathVariable Long id, @RequestBody java.util.Map<String, String> params) {
        Notice existing = noticeService.getById(id);
        if (existing == null) {
            throw new BusinessException("公告不存在");
        }
        checkPublishPerm(existing.getClubId());
        existing.setTop(params.getOrDefault("top", "N"));
        noticeService.updateById(existing);
        return R.success();
    }

    /** 发布权限：平台通知仅管理员；社团通知仅该社团社长或管理员 */
    private void checkPublishPerm(Long clubId) {
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.getLoginUser() != null
                && "ADMIN".equals(SecurityUtils.getLoginUser().getUserType());
        if (isAdmin) {
            return;
        }
        if (clubId == null || clubId == 0L) {
            throw new BusinessException("平台公告仅管理员可发布");
        }
        ClubMember member = clubMemberService.getMember(clubId, userId);
        if (member == null || !"PRESIDENT".equals(member.getMemberRole())) {
            throw new BusinessException("仅该社团社长或管理员可管理公告");
        }
    }
}
