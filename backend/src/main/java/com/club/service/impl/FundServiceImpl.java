package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.BusinessException;
import com.club.domain.Fund;
import com.club.domain.FundRecord;
import com.club.enums.FundStatus;
import com.club.enums.FundType;
import com.club.mapper.FundMapper;
import com.club.mapper.FundRecordMapper;
import com.club.service.FundService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class FundServiceImpl extends ServiceImpl<FundMapper, Fund> implements FundService {

    @Resource
    private FundRecordMapper fundRecordMapper;

    @Override
    public IPage<Fund> listPage(Integer pageNum, Integer pageSize, Long clubId, String status) {
        LambdaQueryWrapper<Fund> wrapper = new LambdaQueryWrapper<>();
        if (clubId != null) wrapper.eq(Fund::getClubId, clubId);
        if (StringUtils.hasText(status)) wrapper.eq(Fund::getStatus, status);
        wrapper.orderByDesc(Fund::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional
    public Long applyFund(Fund fund) {
        if (fund.getAmount() == null || fund.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("金额必须大于0");
        }
        fund.setStatus(FundStatus.PENDING.name());
        save(fund);
        return fund.getId();
    }

    @Override
    @Transactional
    public void auditFund(Long fundId, boolean approved, String remark, Long auditUserId) {
        Fund fund = getById(fundId);
        if (fund == null) throw new BusinessException("经费申请不存在");
        if (!FundStatus.PENDING.name().equals(fund.getStatus())) {
            throw new BusinessException("仅待审批状态可审批");
        }
        fund.setStatus(approved ? FundStatus.APPROVED.name() : FundStatus.REJECTED.name());
        fund.setAuditUserId(auditUserId);
        fund.setAuditTime(LocalDateTime.now());
        fund.setAuditRemark(remark);
        updateById(fund);
        // 审批通过时生成流水
        if (approved) {
            BigDecimal balance = getBalance(fund.getClubId());
            BigDecimal newBalance = FundType.INCOME.name().equals(fund.getType())
                    ? balance.add(fund.getAmount())
                    : balance.subtract(fund.getAmount());
            FundRecord record = new FundRecord();
            record.setFundId(fundId);
            record.setClubId(fund.getClubId());
            record.setAmount(fund.getAmount());
            record.setType(fund.getType());
            record.setBalanceAfter(newBalance);
            fundRecordMapper.insert(record);
        }
    }

    @Override
    public BigDecimal getBalance(Long clubId) {
        LambdaQueryWrapper<FundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FundRecord::getClubId, clubId);
        wrapper.orderByDesc(FundRecord::getCreateTime);
        wrapper.last("LIMIT 1");
        FundRecord latest = fundRecordMapper.selectOne(wrapper);
        return latest != null ? latest.getBalanceAfter() : BigDecimal.ZERO;
    }

    @Override
    public IPage<FundRecord> listRecords(Long clubId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<FundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FundRecord::getClubId, clubId);
        wrapper.orderByDesc(FundRecord::getCreateTime);
        return fundRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
