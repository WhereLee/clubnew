package com.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.Activity;
import com.club.domain.ActivityCheckin;
import com.club.domain.ActivitySignup;

public interface ActivityService extends IService<Activity> {
    IPage<Activity> listPage(Integer pageNum, Integer pageSize, Long clubId, String status);
    Long createActivity(Activity activity);
    void signupActivity(Long activityId, Long userId);
    void cancelSignup(Long signupId, Long userId);
    void checkin(Long activityId, Long userId);
    IPage<ActivityCheckin> listCheckins(Long activityId, Integer pageNum, Integer pageSize);
    void submitAudit(Long activityId);
    void auditActivity(Long activityId, boolean approved, String remark);
    void publishActivity(Long activityId);
    void cancelActivity(Long activityId);
}
