-- V2：辅助索引与数据完整性约束
-- 目标：覆盖高频过滤组合，避免全表扫描；金额在 DB 层兜底校验

-- 社团成员：按社团查成员 + 状态过滤（成员列表/审批查询）
CREATE INDEX idx_club_member_club_status ON club_member (club_id, status);

-- 纳新报名记录：按纳新 + 状态过滤（报名记录列表）
CREATE INDEX idx_recruit_record_recruit_status ON recruit_record (recruit_id, status);

-- 活动报名：按活动 + 状态过滤
CREATE INDEX idx_activity_signup_activity_status ON activity_signup (activity_id, status);

-- 经费申请：按社团 + 状态过滤（审批列表）
CREATE INDEX idx_fund_club_status ON fund (club_id, status);

-- 经费流水：按社团 + 时间排序（流水查询）
CREATE INDEX idx_fund_record_club_time ON fund_record (club_id, create_time);

-- 动态：按社团 + 时间倒序（广场/社团动态流）
CREATE INDEX idx_post_club_time ON post (club_id, create_time);

-- 评论：业务类型 + 业务ID（评论列表）——V1 已建 idx_biz(biz_type, biz_id)，此处不重复
-- （原 idx_comment_biz 与 V1 idx_biz 完全同义，已移除）

-- 公告：社团 + 置顶 + 状态（公告列表）
CREATE INDEX idx_notice_club_top ON notice (club_id, top, status);

-- 金额 DB 兜底：经费申请金额必须为正数（应用层校验之外的最后一层防线）
-- MySQL 8.0.16+ / H2 均支持 CHECK 约束
ALTER TABLE fund ADD CONSTRAINT chk_fund_amount_positive CHECK (amount > 0);
