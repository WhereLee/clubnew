package com.club.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.club.annotation.Log;
import com.club.annotation.RateLimiter;
import com.club.annotation.RepeatSubmit;
import com.club.common.R;
import com.club.domain.Activity;
import com.club.domain.ActivityCheckin;
import com.club.dto.ActivityAuditDTO;
import com.club.dto.ActivityDTO;
import com.club.security.SecurityUtils;
import com.club.service.ActivityService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Resource
    private ActivityService activityService;

    @PostMapping
    @PreAuthorize("@ss.hasPermi('activity:manage')")
    @Log(title = "活动管理", businessType = 1)
    public R<Long> create(@Valid @RequestBody ActivityDTO dto) {
        Activity activity = new Activity();
        activity.setClubId(dto.getClubId());
        activity.setTitle(dto.getTitle());
        activity.setContent(dto.getContent());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setQuota(dto.getQuota());
        activity.setCheckinEnabled(dto.getCheckinEnabled());
        return R.success(activityService.createActivity(activity));
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('activity:manage')")
    @Log(title = "活动管理", businessType = 2)
    public R<Void> update(@Valid @RequestBody ActivityDTO dto) {
        return R.success();
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("@ss.hasPermi('activity:manage')")
    @Log(title = "活动提交审核", businessType = 2)
    public R<Void> submit(@PathVariable Long id) {
        activityService.submitAudit(id);
        return R.success();
    }

    @PutMapping("/{id}/audit")
    @PreAuthorize("@ss.hasPermi('activity:manage')")
    @Log(title = "活动审核", businessType = 2)
    public R<Void> audit(@PathVariable Long id, @Valid @RequestBody ActivityAuditDTO dto) {
        activityService.auditActivity(id, dto.getApproved(), dto.getRemark());
        return R.success();
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("@ss.hasPermi('activity:manage')")
    @Log(title = "活动发布", businessType = 2)
    public R<Void> publish(@PathVariable Long id) {
        activityService.publishActivity(id);
        return R.success();
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("@ss.hasPermi('activity:manage')")
    @Log(title = "活动取消", businessType = 3)
    public R<Void> cancel(@PathVariable Long id) {
        activityService.cancelActivity(id);
        return R.success();
    }

    @GetMapping("/list")
    public R<IPage<Activity>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   Long clubId, String status) {
        return R.success(activityService.listPage(pageNum, pageSize, clubId, status));
    }

    @GetMapping("/{id}")
    public R<Activity> getById(@PathVariable Long id) {
        return R.success(activityService.getById(id));
    }

    @PostMapping("/{id}/signup")
    @RepeatSubmit(interval = 3000)
    @RateLimiter(key = "activity:signup", count = 100, time = 60)
    @Log(title = "活动报名", businessType = 1)
    public R<Void> signup(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        activityService.signupActivity(id, userId);
        return R.success();
    }

    @PutMapping("/signup/{signupId}/cancel")
    @Log(title = "取消活动报名", businessType = 3)
    public R<Void> cancelSignup(@PathVariable Long signupId) {
        Long userId = SecurityUtils.getUserId();
        activityService.cancelSignup(signupId, userId);
        return R.success();
    }

    @PostMapping("/{id}/checkin")
    @RepeatSubmit(interval = 3000)
    @Log(title = "活动签到", businessType = 1)
    public R<Void> checkin(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        activityService.checkin(id, userId);
        return R.success();
    }

    @GetMapping("/{id}/checkin/list")
    public R<IPage<ActivityCheckin>> checkinList(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.success(activityService.listCheckins(id, pageNum, pageSize));
    }
}
