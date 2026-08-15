package com.club.controller;

import com.club.annotation.DataScope;
import com.club.annotation.Log;
import com.club.common.BusinessException;
import com.club.common.R;
import com.club.domain.Club;
import com.club.domain.ClubMember;
import com.club.dto.ClubAuditDTO;
import com.club.enums.ClubStatus;
import com.club.metrics.ClubMetrics;
import com.club.security.SecurityUtils;
import com.club.service.ClubMemberService;
import com.club.service.ClubService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/club")
public class ClubController {

    @Resource
    private ClubService clubService;

    @Resource
    private ClubMemberService clubMemberService;

    @Resource
    private ClubMetrics clubMetrics;

    @PostMapping("/apply")
    @Log(title = "社团管理", businessType = 1)
    public R<Long> apply(@RequestBody Club club) {
        Long userId = SecurityUtils.getUserId();
        return R.success(clubService.applyClub(club, userId));
    }

    @GetMapping("/list")
    @DataScope(userAlias = "club")
    public R<IPage<Club>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               String name, String category, String status) {
        return R.success(clubService.listPage(pageNum, pageSize, name, category, status));
    }

    @GetMapping("/{id}")
    public R<Club> getById(@PathVariable Long id) {
        return R.success(clubService.getById(id));
    }

    @PutMapping
    @Log(title = "社团管理", businessType = 2)
    public R<Void> update(@RequestBody Club club) {
        Club existing = clubService.getById(club.getId());
        if (existing == null) throw new BusinessException("社团不存在");
        if (!ClubStatus.APPROVED.name().equals(existing.getStatus())) {
            throw new BusinessException("仅运营中的社团可修改");
        }
        // 权限校验：仅社长或管理员可修改
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.getLoginUser() != null && "ADMIN".equals(SecurityUtils.getLoginUser().getUserType());
        if (!isAdmin) {
            ClubMember member = clubMemberService.getMember(club.getId(), userId);
            if (member == null || !"PRESIDENT".equals(member.getMemberRole())) {
                throw new BusinessException("仅社长或管理员可修改社团信息");
            }
        }
        clubService.updateById(club);
        return R.success();
    }

    @PutMapping("/audit")
    @PreAuthorize("@ss.hasPermi('club:audit')")
    @Log(title = "社团审批", businessType = 2)
    public R<Void> audit(@Valid @RequestBody ClubAuditDTO dto) {
        Long auditUserId = SecurityUtils.getUserId();
        clubService.auditClub(dto.getClubId(), dto.getApproved(), dto.getRemark(), auditUserId);
        if (Boolean.TRUE.equals(dto.getApproved())) {
            clubMetrics.incrClubAuditApproved();
        } else {
            clubMetrics.incrClubAuditRejected();
        }
        return R.success();
    }

    @PutMapping("/suspend/{id}")
    @PreAuthorize("@ss.hasPermi('club:audit')")
    @Log(title = "社团管理", businessType = 2)
    public R<Void> suspend(@PathVariable Long id) {
        clubService.changeStatus(id, ClubStatus.SUSPENDED.name());
        return R.success();
    }

    @PutMapping("/resume/{id}")
    @PreAuthorize("@ss.hasPermi('club:audit')")
    @Log(title = "社团管理", businessType = 2)
    public R<Void> resume(@PathVariable Long id) {
        clubService.changeStatus(id, ClubStatus.APPROVED.name());
        return R.success();
    }

    @PutMapping("/dissolve/{id}")
    @PreAuthorize("@ss.hasPermi('club:audit')")
    @Log(title = "社团管理", businessType = 3)
    public R<Void> dissolve(@PathVariable Long id) {
        clubService.changeStatus(id, ClubStatus.DISSOLVED.name());
        return R.success();
    }

    @PutMapping("/star/{id}")
    @PreAuthorize("@ss.hasPermi('club:audit')")
    @Log(title = "社团管理", businessType = 2)
    public R<Void> star(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer starLevel = params.get("starLevel");
        if (starLevel == null || starLevel < 1 || starLevel > 5) throw new BusinessException("星级必须在1-5之间");
        Club club = clubService.getById(id);
        club.setStarLevel(starLevel);
        clubService.updateById(club);
        return R.success();
    }

    @GetMapping("/mine")
    public R<Club> mine() {
        Long userId = SecurityUtils.getUserId();
        return R.success(clubService.getByUserId(userId));
    }

    @GetMapping("/{id}/members")
    public R<IPage<ClubMember>> members(@PathVariable Long id,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.success(clubMemberService.listByClubId(id, pageNum, pageSize));
    }
}
