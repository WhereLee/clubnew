package com.club.controller;

import com.club.annotation.Log;
import com.club.common.BusinessException;
import com.club.common.R;
import com.club.domain.ClubMember;
import com.club.dto.MemberApplyDTO;
import com.club.dto.MemberAuditDTO;
import com.club.dto.MemberRoleDTO;
import com.club.dto.TransferPresidentDTO;
import com.club.enums.MemberRole;
import com.club.enums.MemberStatus;
import com.club.security.SecurityUtils;
import com.club.service.ClubMemberService;
import com.club.service.ClubService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/club/member")
public class ClubMemberController {

    @Resource
    private ClubMemberService clubMemberService;

    @Resource
    private ClubService clubService;

    /** 校验操作者是否为社团社长或副社长 */
    private void checkPresidentOrVice(Long clubId) {
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.getLoginUser() != null && "ADMIN".equals(SecurityUtils.getLoginUser().getUserType());
        if (isAdmin) return;
        ClubMember member = clubMemberService.getMember(clubId, userId);
        if (member == null || (!MemberRole.PRESIDENT.name().equals(member.getMemberRole())
                && !MemberRole.VICE.name().equals(member.getMemberRole()))) {
            throw new BusinessException("仅社长或副社长可执行此操作");
        }
    }

    @PostMapping("/apply")
    @Log(title = "成员管理", businessType = 1)
    public R<Void> apply(@Valid @RequestBody MemberApplyDTO dto) {
        Long userId = SecurityUtils.getUserId();
        clubMemberService.applyMember(dto.getClubId(), userId);
        return R.success();
    }

    @PutMapping("/audit")
    @Log(title = "成员管理", businessType = 2)
    public R<Void> audit(@Valid @RequestBody MemberAuditDTO dto) {
        // 校验操作者是社长/副社
        ClubMember target = clubMemberService.getById(dto.getMemberId());
        if (target == null) throw new BusinessException("申请记录不存在");
        checkPresidentOrVice(target.getClubId());
        Long auditUserId = SecurityUtils.getUserId();
        clubMemberService.auditMember(dto.getMemberId(), dto.getApproved(), auditUserId);
        return R.success();
    }

    @PutMapping("/quit/{clubId}")
    @Log(title = "成员管理", businessType = 3)
    public R<Void> quit(@PathVariable Long clubId) {
        Long userId = SecurityUtils.getUserId();
        clubMemberService.quitClub(clubId, userId);
        return R.success();
    }

    @PutMapping("/remove/{memberId}")
    @Log(title = "成员管理", businessType = 3)
    public R<Void> remove(@PathVariable Long memberId) {
        ClubMember target = clubMemberService.getById(memberId);
        if (target == null) throw new BusinessException("成员不存在");
        checkPresidentOrVice(target.getClubId());
        Long operatorId = SecurityUtils.getUserId();
        clubMemberService.removeMember(memberId, operatorId);
        return R.success();
    }

    @PutMapping("/role/{memberId}")
    @Log(title = "成员管理", businessType = 2)
    public R<Void> changeRole(@PathVariable Long memberId, @Valid @RequestBody MemberRoleDTO dto) {
        ClubMember target = clubMemberService.getById(memberId);
        if (target == null) throw new BusinessException("成员不存在");
        checkPresidentOrVice(target.getClubId());
        Long operatorId = SecurityUtils.getUserId();
        clubMemberService.changeRole(memberId, dto.getMemberRole(), operatorId);
        return R.success();
    }

    @PutMapping("/transfer/president")
    @Log(title = "换届", businessType = 2)
    public R<Void> transferPresident(@Valid @RequestBody TransferPresidentDTO dto) {
        // 校验操作者是现任社长
        Long userId = SecurityUtils.getUserId();
        ClubMember operator = clubMemberService.getMember(dto.getClubId(), userId);
        if (operator == null || !MemberRole.PRESIDENT.name().equals(operator.getMemberRole())) {
            throw new BusinessException("仅社长可执行换届操作");
        }
        clubService.transferPresident(dto.getClubId(), dto.getNewPresidentUserId());
        return R.success();
    }
}
