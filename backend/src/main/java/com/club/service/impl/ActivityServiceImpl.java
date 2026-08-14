package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.BusinessException;
import com.club.domain.Activity;
import com.club.domain.ActivityCheckin;
import com.club.domain.ActivitySignup;
import com.club.enums.ActivityStatus;
import com.club.event.ClubEventPublisher;
import com.club.event.EventType;
import com.club.mapper.ActivityCheckinMapper;
import com.club.mapper.ActivityMapper;
import com.club.mapper.ActivitySignupMapper;
import com.club.metrics.ClubMetrics;
import com.club.service.ActivityService;
import com.club.service.RedisStockService;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private ActivitySignupMapper signupMapper;

    @Resource
    private ActivityCheckinMapper checkinMapper;

    @Resource
    private ClubEventPublisher eventPublisher;

    @Resource
    private RedisStockService stockService;

    @Resource
    private ClubMetrics metrics;

    private static final String STOCK_KEY_PREFIX = "stock:activity:";

    @Override
    public IPage<Activity> listPage(Integer pageNum, Integer pageSize, Long clubId, String status) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (clubId != null) wrapper.eq(Activity::getClubId, clubId);
        if (status != null) wrapper.eq(Activity::getStatus, status);
        wrapper.orderByDesc(Activity::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional
    public Long createActivity(Activity activity) {
        activity.setStatus(ActivityStatus.DRAFT.name());
        activity.setAppliedCount(0);
        activity.setVersion(0);
        if (activity.getCheckinEnabled() == null) activity.setCheckinEnabled("N");
        save(activity);
        // 初始化 Redis 库存
        try {
            int quota = activity.getQuota() == null ? 0 : activity.getQuota();
            stockService.initStock(STOCK_KEY_PREFIX + activity.getId(), quota);
        } catch (Exception ignored) {
            // Redis 不可用时由 DB 原子扣减兜底
        }
        return activity.getId();
    }

    @Override
    @Transactional
    public void submitAudit(Long activityId) {
        Activity activity = getById(activityId);
        if (activity == null) throw new BusinessException("活动不存在");
        if (!ActivityStatus.DRAFT.name().equals(activity.getStatus())) {
            throw new BusinessException("仅草稿可提交审核");
        }
        activity.setStatus(ActivityStatus.PENDING.name());
        updateById(activity);
    }

    @Override
    @Transactional
    public void auditActivity(Long activityId, boolean approved, String remark) {
        Activity activity = getById(activityId);
        if (activity == null) throw new BusinessException("活动不存在");
        if (!ActivityStatus.PENDING.name().equals(activity.getStatus())) {
            throw new BusinessException("仅待审核状态可审批");
        }
        if (approved) {
            activity.setStatus(ActivityStatus.PUBLISHED.name());
        } else {
            activity.setStatus(ActivityStatus.CANCELLED.name());
        }
        updateById(activity);
    }

    @Override
    @Transactional
    public void publishActivity(Long activityId) {
        Activity activity = getById(activityId);
        if (activity == null) throw new BusinessException("活动不存在");
        if (!ActivityStatus.PENDING.name().equals(activity.getStatus())) {
            throw new BusinessException("仅待审核状态可发布");
        }
        activity.setStatus(ActivityStatus.PUBLISHED.name());
        updateById(activity);
    }

    @Override
    @Transactional
    public void cancelActivity(Long activityId) {
        Activity activity = getById(activityId);
        if (activity == null) throw new BusinessException("活动不存在");
        String s = activity.getStatus();
        if (ActivityStatus.ENDED.name().equals(s) || ActivityStatus.CANCELLED.name().equals(s)) {
            throw new BusinessException("终态不可取消");
        }
        activity.setStatus(ActivityStatus.CANCELLED.name());
        updateById(activity);
    }

    @Override
    @Transactional
    public void signupActivity(Long activityId, Long userId) {
        metrics.incrActivitySignup();
        Activity activity = getById(activityId);
        if (activity == null) throw new BusinessException("活动不存在");
        String status = activity.getStatus();
        if (!ActivityStatus.PUBLISHED.name().equals(status) && !ActivityStatus.ONGOING.name().equals(status)) {
            throw new BusinessException("活动未开放报名");
        }
        // 时间窗口校验：开始前不可报
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime() != null && now.isBefore(activity.getStartTime())) {
            throw new BusinessException("活动尚未开始");
        }
        // 结束后不可报
        if (activity.getEndTime() != null && now.isAfter(activity.getEndTime())) {
            throw new BusinessException("活动已结束");
        }
        // 重复报名
        LambdaQueryWrapper<ActivitySignup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivitySignup::getActivityId, activityId)
               .eq(ActivitySignup::getUserId, userId)
               .eq(ActivitySignup::getStatus, "SIGNED");
        if (signupMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("已报名，请勿重复提交");
        }
        // Redis 预扣库存（1=成功，0=不足，-1=降级走 DB）
        String stockKey = STOCK_KEY_PREFIX + activityId;
        int preDeduct = stockService.tryDeduct(stockKey);
        if (preDeduct == 0) throw new BusinessException("名额已满");
        // 数据库原子扣减（最终防线，防超卖）
        int rows = activityMapper.applyActivity(activityId);
        if (rows == 0) {
            stockService.rollback(stockKey);
            throw new BusinessException("名额已满");
        }
        ActivitySignup signup = new ActivitySignup();
        signup.setActivityId(activityId);
        signup.setUserId(userId);
        signup.setStatus("SIGNED");
        signup.setSignupTime(now);
        signup.setVersion(0);
        try {
            signupMapper.insert(signup);
        } catch (DuplicateKeyException e) {
            stockService.rollback(stockKey);
            throw new BusinessException("已报名，请勿重复提交");
        }
        // 异步加分
        eventPublisher.publish(EventType.ACTIVITY_SIGNUP, activity.getClubId(), userId, activity.getId(), null);
    }

    @Override
    @Transactional
    public void cancelSignup(Long signupId, Long userId) {
        ActivitySignup signup = signupMapper.selectById(signupId);
        if (signup == null) throw new BusinessException("报名记录不存在");
        if (!signup.getUserId().equals(userId)) throw new BusinessException("只能取消自己的报名");
        signup.setStatus("CANCELLED");
        signupMapper.updateById(signup);
        activityMapper.cancelApply(signup.getActivityId());
    }

    @Override
    @Transactional
    public void checkin(Long activityId, Long userId) {
        Activity activity = getById(activityId);
        if (activity == null) throw new BusinessException("活动不存在");
        if (!"Y".equals(activity.getCheckinEnabled())) throw new BusinessException("活动未开启签到");
        // 签到时间窗口校验
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime() != null && now.isBefore(activity.getStartTime())) {
            throw new BusinessException("活动尚未开始，不能签到");
        }
        if (activity.getEndTime() != null && now.isAfter(activity.getEndTime())) {
            throw new BusinessException("活动已结束，不能签到");
        }
        // 校验是否已报名
        LambdaQueryWrapper<ActivitySignup> signupWrapper = new LambdaQueryWrapper<>();
        signupWrapper.eq(ActivitySignup::getActivityId, activityId)
                     .eq(ActivitySignup::getUserId, userId)
                     .eq(ActivitySignup::getStatus, "SIGNED");
        if (signupMapper.selectCount(signupWrapper) == 0) {
            throw new BusinessException("未报名，不能签到");
        }
        // 幂等：已签到则返回
        LambdaQueryWrapper<ActivityCheckin> checkinWrapper = new LambdaQueryWrapper<>();
        checkinWrapper.eq(ActivityCheckin::getActivityId, activityId)
                      .eq(ActivityCheckin::getUserId, userId);
        if (checkinMapper.selectCount(checkinWrapper) > 0) {
            return; // 已签到，幂等返回
        }
        ActivityCheckin checkin = new ActivityCheckin();
        checkin.setActivityId(activityId);
        checkin.setUserId(userId);
        checkin.setCheckinTime(now);
        checkin.setStatus("1");
        try {
            checkinMapper.insert(checkin);
            // 签到成功：发布活跃度事件（异步消费加分）
            eventPublisher.publish(EventType.ACTIVITY_CHECKIN, activity.getClubId(), userId, activityId, null);
        } catch (DuplicateKeyException e) {
            // 并发重复签到：唯一约束(activity_id, user_id)兜底，幂等返回
            return;
        }
    }

    @Override
    public IPage<ActivityCheckin> listCheckins(Long activityId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ActivityCheckin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityCheckin::getActivityId, activityId);
        return checkinMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

}
