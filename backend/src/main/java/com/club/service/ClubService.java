package com.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.Club;

public interface ClubService extends IService<Club> {

    IPage<Club> listPage(Integer pageNum, Integer pageSize, String name, String category, String status);

    Long applyClub(Club club, Long userId);

    void auditClub(Long clubId, boolean approved, String remark, Long auditUserId);

    void changeStatus(Long clubId, String targetStatus);

    void transferPresident(Long clubId, Long newPresidentUserId);

    Club getByUserId(Long userId);
}
