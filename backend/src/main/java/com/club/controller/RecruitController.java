package com.club.controller;

import com.club.annotation.Log;
import com.club.annotation.RateLimiter;
import com.club.annotation.RepeatSubmit;
import com.club.common.BusinessException;
import com.club.common.R;
import com.club.dto.RecruitDTO;
import com.club.dto.RecruitRecordResultDTO;
import com.club.security.SecurityUtils;
import com.club.service.RecruitService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.club.domain.Recruit;
import com.club.domain.RecruitRecord;
import com.baomidou.mybatisplus.core.metadata.IPage;

@RestController
@RequestMapping("/recruit")
public class RecruitController {

    @Resource
    private RecruitService recruitService;

    @PostMapping
    @PreAuthorize("@ss.hasPermi('recruit:manage')")
    @Log(title = "纳新管理", businessType = 1)
    public R<Long> create(@Valid @RequestBody RecruitDTO dto) {
        Recruit recruit = new Recruit();
        recruit.setClubId(dto.getClubId());
        recruit.setTitle(dto.getTitle());
        recruit.setDescription(dto.getDescription());
        recruit.setQuota(dto.getQuota());
        recruit.setStartTime(dto.getStartTime());
        recruit.setEndTime(dto.getEndTime());
        recruit.setRequirements(dto.getRequirements());
        return R.success(recruitService.createRecruit(recruit));
    }

    @GetMapping("/list")
    public R<IPage<Recruit>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  Long clubId, String status) {
        return R.success(recruitService.listPage(pageNum, pageSize, clubId, status));
    }

    @GetMapping("/{id}")
    public R<Recruit> getById(@PathVariable Long id) {
        return R.success(recruitService.getById(id));
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('recruit:manage')")
    @Log(title = "纳新管理", businessType = 2)
    public R<Void> update(@Valid @RequestBody RecruitDTO dto) {
        Recruit recruit = new Recruit();
        recruit.setId(dto.getId());
        recruit.setTitle(dto.getTitle());
        recruit.setDescription(dto.getDescription());
        recruit.setQuota(dto.getQuota());
        recruit.setStartTime(dto.getStartTime());
        recruit.setEndTime(dto.getEndTime());
        recruit.setRequirements(dto.getRequirements());
        recruitService.updateRecruit(recruit);
        return R.success();
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("@ss.hasPermi('recruit:manage')")
    @Log(title = "纳新管理", businessType = 3)
    public R<Void> cancel(@PathVariable Long id) {
        recruitService.cancelRecruit(id);
        return R.success();
    }

    @PostMapping("/{id}/apply")
    @RepeatSubmit(interval = 3000)
    @RateLimiter(key = "recruit:apply", count = 100, time = 60)
    @Log(title = "纳新报名", businessType = 1)
    public R<Void> apply(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        recruitService.applyRecruit(id, userId);
        return R.success();
    }

    @PutMapping("/record/{recordId}/cancel")
    @Log(title = "取消报名", businessType = 3)
    public R<Void> cancelRecord(@PathVariable Long recordId) {
        Long userId = SecurityUtils.getUserId();
        recruitService.cancelApply(recordId, userId);
        return R.success();
    }

    @PutMapping("/record/{recordId}/result")
    @PreAuthorize("@ss.hasPermi('recruit:manage')")
    @Log(title = "面试结果", businessType = 2)
    public R<Void> recordResult(@PathVariable Long recordId, @Valid @RequestBody RecruitRecordResultDTO dto) {
        recruitService.auditRecord(recordId, dto.getPassed(), dto.getResult());
        return R.success();
    }

    @GetMapping("/{id}/records")
    public R<IPage<RecruitRecord>> records(@PathVariable Long id,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.success(recruitService.listRecords(id, pageNum, pageSize));
    }
}
