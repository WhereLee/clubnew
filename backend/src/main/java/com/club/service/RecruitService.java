package com.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.Recruit;
import com.club.domain.RecruitRecord;

public interface RecruitService extends IService<Recruit> {
    IPage<Recruit> listPage(Integer pageNum, Integer pageSize, Long clubId, String status);
    Long createRecruit(Recruit recruit);
    void updateRecruit(Recruit recruit);
    void applyRecruit(Long recruitId, Long userId);
    void cancelApply(Long recordId, Long userId);
    void auditRecord(Long recordId, boolean passed, String result);
    IPage<RecruitRecord> listRecords(Long recruitId, Integer pageNum, Integer pageSize);
    void cancelRecruit(Long recruitId);
}
