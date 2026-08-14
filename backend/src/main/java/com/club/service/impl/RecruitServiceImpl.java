package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.BusinessException;
import com.club.domain.Recruit;
import com.club.domain.RecruitRecord;
import com.club.enums.RecruitRecordStatus;
import com.club.enums.RecruitStatus;
import com.club.event.ClubEventPublisher;
import com.club.event.EventType;
import com.club.mapper.RecruitMapper;
import com.club.metrics.ClubMetrics;
import com.club.mapper.RecruitRecordMapper;
import com.club.service.RecruitService;
import com.club.service.RedisStockService;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RecruitServiceImpl extends ServiceImpl<RecruitMapper, Recruit> implements RecruitService {

    @Resource
    private RecruitMapper recruitMapper;

    @Resource
    private RecruitRecordMapper recruitRecordMapper;

    @Resource
    private RedisStockService stockService;

    @Resource
    private ClubEventPublisher eventPublisher;

    @Resource
    private ClubMetrics metrics;

    private static final String STOCK_KEY_PREFIX = "stock:recruit:";

    @Override
    public IPage<Recruit> listPage(Integer pageNum, Integer pageSize, Long clubId, String status) {
        LambdaQueryWrapper<Recruit> wrapper = new LambdaQueryWrapper<>();
        if (clubId != null) wrapper.eq(Recruit::getClubId, clubId);
        if (status != null) wrapper.eq(Recruit::getStatus, status);
        wrapper.orderByDesc(Recruit::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional
    public Long createRecruit(Recruit recruit) {
        recruit.setStatus(RecruitStatus.NOT_STARTED.name());
        recruit.setAppliedCount(0);
        recruit.setVersion(0);
        save(recruit);
        // 初始化 Redis 库存
        try {
            stockService.initStock(STOCK_KEY_PREFIX + recruit.getId(), recruit.getQuota());
        } catch (Exception ignored) {
            // Redis 不可用时由 DB 原子扣减兜底
        }
        return recruit.getId();
    }

    @Override
    @Transactional
    public void updateRecruit(Recruit recruit) {
        if (recruit.getId() == null) {
            throw new BusinessException("纳新ID不能为空");
        }
        Recruit existing = getById(recruit.getId());
        if (existing == null) {
            throw new BusinessException("纳新不存在");
        }
        // 仅未开始可修改
        if (!RecruitStatus.NOT_STARTED.name().equals(existing.getStatus())) {
            throw new BusinessException("仅未开始的纳新可修改");
        }
        // 只允许更新可编辑字段，clubId/status/applied_count 由系统维护
        Recruit update = new Recruit();
        update.setId(existing.getId());
        update.setTitle(recruit.getTitle());
        update.setDescription(recruit.getDescription());
        update.setQuota(recruit.getQuota());
        update.setStartTime(recruit.getStartTime());
        update.setEndTime(recruit.getEndTime());
        update.setRequirements(recruit.getRequirements());
        updateById(update);
    }

    @Override
    @Transactional
    public void applyRecruit(Long recruitId, Long userId) {
        Recruit recruit = getById(recruitId);
        if (recruit == null) throw new BusinessException("纳新不存在");
        if (!RecruitStatus.IN_PROGRESS.name().equals(recruit.getStatus())) {
            throw new BusinessException("纳新未在进行中");
        }
        // 时间窗口校验
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(recruit.getStartTime())) throw new BusinessException("纳新尚未开始");
        if (now.isAfter(recruit.getEndTime())) throw new BusinessException("纳新已结束");
        // 重复报名校验
        LambdaQueryWrapper<RecruitRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecruitRecord::getRecruitId, recruitId)
               .eq(RecruitRecord::getUserId, userId)
               .in(RecruitRecord::getStatus, RecruitRecordStatus.PENDING.name(), RecruitRecordStatus.PASSED.name());
        if (recruitRecordMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("已报名，请勿重复提交");
        }
        metrics.incrRecruitApply();
        // Redis 预扣库存（1=成功，0=不足，-1=降级走 DB）
        String stockKey = STOCK_KEY_PREFIX + recruitId;
        int preDeduct = stockService.tryDeduct(stockKey);
        if (preDeduct == 0) {
            metrics.incrStockPreDeductFailures();
            throw new BusinessException("名额已满");
        }
        // 数据库原子扣减（最终防线，防超卖）
        int rows = recruitMapper.applyRecruit(recruitId);
        if (rows == 0) {
            // DB 扣减失败，回滚 Redis 预扣
            metrics.incrStockPreDeductFailures();
            stockService.rollback(stockKey);
            throw new BusinessException("名额已满");
        }
        // 写入报名记录（唯一约束兜底并发重复报名）
        RecruitRecord record = new RecruitRecord();
        record.setRecruitId(recruitId);
        record.setUserId(userId);
        record.setStatus(RecruitRecordStatus.PENDING.name());
        record.setApplyTime(now);
        record.setVersion(0);
        try {
            recruitRecordMapper.insert(record);
            // 报名成功：发布纳新活跃度事件（Stream 异步消费加分）
            eventPublisher.publish(EventType.RECRUIT_APPLY, recruit.getClubId(), userId, record.getId(), null);
        } catch (DuplicateKeyException e) {
            // 并发重复报名：回滚 Redis 预扣，抛友好异常（DB 名额扣减随事务回滚）
            stockService.rollback(stockKey);
            throw new BusinessException("已报名，请勿重复提交");
        }
    }

    @Override
    @Transactional
    public void cancelApply(Long recordId, Long userId) {
        RecruitRecord record = recruitRecordMapper.selectById(recordId);
        if (record == null) throw new BusinessException("报名记录不存在");
        if (!record.getUserId().equals(userId)) throw new BusinessException("只能取消自己的报名");
        record.setStatus(RecruitRecordStatus.CANCELLED.name());
        recruitRecordMapper.updateById(record);
        // 回补名额
        recruitMapper.cancelApply(record.getRecruitId());
    }

    @Override
    @Transactional
    public void auditRecord(Long recordId, boolean passed, String result) {
        RecruitRecord record = recruitRecordMapper.selectById(recordId);
        if (record == null) throw new BusinessException("记录不存在");
        record.setStatus(passed ? RecruitRecordStatus.PASSED.name() : RecruitRecordStatus.FAILED.name());
        record.setInterviewResult(result);
        recruitRecordMapper.updateById(record);
    }

    @Override
    public IPage<RecruitRecord> listRecords(Long recruitId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<RecruitRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecruitRecord::getRecruitId, recruitId);
        return recruitRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional
    public void cancelRecruit(Long recruitId) {
        Recruit recruit = getById(recruitId);
        if (recruit == null) throw new BusinessException("纳新不存在");
        String s = recruit.getStatus();
        if (RecruitStatus.ENDED.name().equals(s) || RecruitStatus.CANCELLED.name().equals(s)) {
            throw new BusinessException("终态不可取消");
        }
        recruit.setStatus(RecruitStatus.CANCELLED.name());
        updateById(recruit);
    }
}
