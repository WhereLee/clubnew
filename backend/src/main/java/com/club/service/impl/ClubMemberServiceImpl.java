package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.BusinessException;
import com.club.domain.ClubMember;
import com.club.enums.MemberRole;
import com.club.enums.MemberStatus;
import com.club.mapper.ClubMemberMapper;
import com.club.security.SecurityUtils;
import com.club.service.ClubMemberService;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ClubMemberServiceImpl extends ServiceImpl<ClubMemberMapper, ClubMember> implements ClubMemberService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public IPage<ClubMember> listByClubId(Long clubId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubMember::getClubId, clubId);
        wrapper.orderByDesc(ClubMember::getJoinTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional
    public void applyMember(Long clubId, Long userId) {
        // 检查是否已申请或已在社
        LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubMember::getClubId, clubId)
               .eq(ClubMember::getUserId, userId)
               .in(ClubMember::getStatus, MemberStatus.PENDING.name(), MemberStatus.ACTIVE.name());
        if (count(wrapper) > 0) {
            throw new BusinessException("已申请或已在社团中");
        }
        ClubMember member = new ClubMember();
        member.setClubId(clubId);
        member.setUserId(userId);
        member.setMemberRole(MemberRole.MEMBER.name());
        member.setStatus(MemberStatus.PENDING.name());
        member.setApplyTime(LocalDateTime.now());
        save(member);
    }

    @Override
    @Transactional
    public void auditMember(Long memberId, boolean approved, Long auditUserId) {
        ClubMember member = getById(memberId);
        if (member == null) {
            throw new BusinessException("申请记录不存在");
        }
        // 权限：仅该社团社长或管理员可审核入社申请（防横向越权）
        assertClubManager(member.getClubId(), auditUserId);
        if (!MemberStatus.PENDING.name().equals(member.getStatus())) {
            throw new BusinessException("该申请已被处理");
        }
        if (approved) {
            member.setStatus(MemberStatus.ACTIVE.name());
            member.setJoinTime(LocalDateTime.now());
            member.setAuditUserId(auditUserId);
            updateById(member);
            // 成员数+1
            jdbcTemplate.update("UPDATE club SET member_count = member_count + 1 WHERE id = ?", member.getClubId());
        } else {
            member.setStatus(MemberStatus.REMOVED.name());
            member.setAuditUserId(auditUserId);
            updateById(member);
        }
    }

    @Override
    @Transactional
    public void quitClub(Long clubId, Long userId) {
        LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubMember::getClubId, clubId)
               .eq(ClubMember::getUserId, userId)
               .eq(ClubMember::getStatus, MemberStatus.ACTIVE.name());
        ClubMember member = getOne(wrapper);
        if (member == null) {
            throw new BusinessException("您不是该社团成员");
        }
        if (MemberRole.PRESIDENT.name().equals(member.getMemberRole())) {
            throw new BusinessException("社长不能直接退出，请先转让社长");
        }
        member.setStatus(MemberStatus.QUIT.name());
        updateById(member);
        // 成员数-1
        jdbcTemplate.update("UPDATE club SET member_count = GREATEST(member_count - 1, 0) WHERE id = ?", clubId);
    }

    @Override
    @Transactional
    public void removeMember(Long memberId, Long operatorId) {
        ClubMember member = getById(memberId);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }
        // 权限：仅该社团社长或管理员可移除成员
        assertClubManager(member.getClubId(), operatorId);
        if (MemberRole.PRESIDENT.name().equals(member.getMemberRole())) {
            throw new BusinessException("不能踢出社长");
        }
        if (!MemberStatus.ACTIVE.name().equals(member.getStatus())) {
            throw new BusinessException("该成员不在社");
        }
        member.setStatus(MemberStatus.REMOVED.name());
        updateById(member);
        // 成员数-1
        jdbcTemplate.update("UPDATE club SET member_count = GREATEST(member_count - 1, 0) WHERE id = ?", member.getClubId());
    }

    @Override
    @Transactional
    public void changeRole(Long memberId, String memberRole, Long operatorId) {
        ClubMember member = getById(memberId);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }
        // 权限：仅该社团社长或管理员可调整成员角色（封「把自己提为社长」的提权路径）
        assertClubManager(member.getClubId(), operatorId);
        if (!MemberStatus.ACTIVE.name().equals(member.getStatus())) {
            throw new BusinessException("该成员不在社");
        }
        // 验证角色值合法
        try {
            MemberRole.valueOf(memberRole);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的角色类型");
        }
        member.setMemberRole(memberRole);
        updateById(member);
    }

    @Override
    public ClubMember getMember(Long clubId, Long userId) {
        LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubMember::getClubId, clubId)
               .eq(ClubMember::getUserId, userId)
               .eq(ClubMember::getStatus, MemberStatus.ACTIVE.name());
        return getOne(wrapper);
    }

    /** 管理权限校验：操作者必须是该社团 ACTIVE 社长，或全局管理员 */
    private void assertClubManager(Long clubId, Long operatorId) {
        if (SecurityUtils.getLoginUser() != null
                && "ADMIN".equals(SecurityUtils.getLoginUser().getUserType())) {
            return;
        }
        ClubMember operator = getMember(clubId, operatorId);
        if (operator == null || !MemberRole.PRESIDENT.name().equals(operator.getMemberRole())) {
            throw new BusinessException("仅社长或管理员可执行此操作");
        }
    }
}
