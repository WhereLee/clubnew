package com.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.ClubMember;

public interface ClubMemberService extends IService<ClubMember> {

    IPage<ClubMember> listByClubId(Long clubId, Integer pageNum, Integer pageSize);

    void applyMember(Long clubId, Long userId);

    void auditMember(Long memberId, boolean approved, Long auditUserId);

    void quitClub(Long clubId, Long userId);

    void removeMember(Long memberId, Long operatorId);

    void changeRole(Long memberId, String memberRole, Long operatorId);

    /** 获取指定社团中的指定用户成员记录 */
    ClubMember getMember(Long clubId, Long userId);
}
