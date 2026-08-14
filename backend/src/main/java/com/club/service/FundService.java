package com.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.Fund;
import com.club.domain.FundRecord;
import java.math.BigDecimal;

public interface FundService extends IService<Fund> {
    IPage<Fund> listPage(Integer pageNum, Integer pageSize, Long clubId, String status);
    Long applyFund(Fund fund);
    void auditFund(Long fundId, boolean approved, String remark, Long auditUserId);
    BigDecimal getBalance(Long clubId);
    IPage<FundRecord> listRecords(Long clubId, Integer pageNum, Integer pageSize);
}
