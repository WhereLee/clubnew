package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.aspect.DataScopeAspect;
import com.club.common.BusinessException;
import com.club.domain.Club;
import com.club.domain.ClubMember;
import com.club.enums.ClubStatus;
import com.club.enums.MemberRole;
import com.club.enums.MemberStatus;
import com.club.mapper.ClubMapper;
import com.club.service.ClubMemberService;
import com.club.service.ClubService;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class ClubServiceImpl extends ServiceImpl<ClubMapper, Club> implements ClubService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private ClubMemberService clubMemberService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private TransactionTemplate transactionTemplate;

    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
        ClubStatus.PENDING.name(), new HashSet<>(Arrays.asList(ClubStatus.APPROVED.name(), ClubStatus.REJECTED.name())),
        ClubStatus.APPROVED.name(), new HashSet<>(Arrays.asList(ClubStatus.SUSPENDED.name(), ClubStatus.DISSOLVED.name())),
        ClubStatus.SUSPENDED.name(), new HashSet<>(Arrays.asList(ClubStatus.APPROVED.name(), ClubStatus.DISSOLVED.name()))
    );

    @Override
    public IPage<Club> listPage(Integer pageNum, Integer pageSize, String name, String category, String status) {
        LambdaQueryWrapper<Club> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) wrapper.like(Club::getName, name);
        if (StringUtils.hasText(category)) wrapper.eq(Club::getCategory, category);
        if (StringUtils.hasText(status)) wrapper.eq(Club::getStatus, status);
        // 消费数据权限切面生成的过滤片段（标注了 @DataScope 的接口才有值）
        String dataScope = DataScopeAspect.getDataScope();
        if (dataScope != null && !dataScope.isEmpty()) {
            wrapper.apply(dataScope);
        }
        wrapper.orderByDesc(Club::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional
    public Long applyClub(Club club, Long userId) {
        // 参数校验
        if (!StringUtils.hasText(club.getName())) throw new BusinessException("社团名称不能为空");
        if (!StringUtils.hasText(club.getCategory())) throw new BusinessException("社团类别不能为空");
        // 检查社团名唯一
        LambdaQueryWrapper<Club> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Club::getName, club.getName());
        if (count(wrapper) > 0) throw new BusinessException("社团名称已存在");
        // 一人只能申请创建一个社团（仅检查待审批状态）
        LambdaQueryWrapper<Club> myClub = new LambdaQueryWrapper<>();
        myClub.eq(Club::getCreateUserId, userId)
              .eq(Club::getStatus, ClubStatus.PENDING.name());
        if (count(myClub) > 0) throw new BusinessException("您已有一个待审批的社团申请");
        // 并发安全生成编号（用时间戳+随机数保证唯一）
        String code = String.format("CLUB%04d", Math.abs((System.nanoTime() % 10000)));
        // 检查编号唯一，冲突则重试
        for (int i = 0; i < 3; i++) {
            LambdaQueryWrapper<Club> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(Club::getCode, code);
            if (count(codeWrapper) == 0) break;
            code = String.format("CLUB%04d", Math.abs(((System.nanoTime() + i) % 10000)));
        }
        club.setCode(code);
        club.setStatus(ClubStatus.PENDING.name());
        club.setCreateUserId(userId);
        club.setApplyTime(LocalDateTime.now());
        club.setMemberCount(0);
        club.setStarLevel(0);
        save(club);
        return club.getId();
    }

    @Override
    @Transactional
    public void auditClub(Long clubId, boolean approved, String remark, Long auditUserId) {
        Club club = getById(clubId);
        if (club == null) throw new BusinessException("社团不存在");
        String targetStatus = approved ? ClubStatus.APPROVED.name() : ClubStatus.REJECTED.name();
        validateTransition(club.getStatus(), targetStatus);
        club.setStatus(targetStatus);
        club.setAuditTime(LocalDateTime.now());
        club.setAuditUserId(auditUserId);
        club.setAuditRemark(remark);
        updateById(club);
        if (approved) {
            club.setPresidentId(club.getCreateUserId());
            updateById(club);
            ClubMember member = new ClubMember();
            member.setClubId(clubId);
            member.setUserId(club.getCreateUserId());
            member.setMemberRole(MemberRole.PRESIDENT.name());
            member.setStatus(MemberStatus.ACTIVE.name());
            member.setJoinTime(LocalDateTime.now());
            member.setAuditUserId(auditUserId);
            clubMemberService.save(member);
            jdbcTemplate.update("UPDATE club SET member_count = member_count + 1 WHERE id = ?", clubId);
        }
    }

    @Override
    @Transactional
    public void changeStatus(Long clubId, String targetStatus) {
        Club club = getById(clubId);
        if (club == null) throw new BusinessException("社团不存在");
        validateTransition(club.getStatus(), targetStatus);
        club.setStatus(targetStatus);
        updateById(club);
    }

    @Override
    public void transferPresident(Long clubId, Long newPresidentUserId) {
        // 使用 Redisson 分布式锁，保证同一社团同一时刻只有一个换届操作
        String lockKey = "lock:club:" + clubId + ":transfer";
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked;
        try {
            locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("换届操作被中断");
        }
        if (!locked) {
            throw new BusinessException("换届操作进行中，请稍后再试");
        }
        try {
            // 在锁内完成事务：事务提交后才释放锁，避免下个线程读到未提交数据
            transactionTemplate.execute(status -> {
                doTransferPresident(clubId, newPresidentUserId);
                return null;
            });
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /** 换届业务逻辑（在锁与事务内执行） */
    private void doTransferPresident(Long clubId, Long newPresidentUserId) {
        Club club = getById(clubId);
        if (club == null) throw new BusinessException("社团不存在");
        if (!ClubStatus.APPROVED.name().equals(club.getStatus())) {
            throw new BusinessException("社团不在运营状态");
        }
        // 检查新社长是否是社团成员
        LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubMember::getClubId, clubId)
               .eq(ClubMember::getUserId, newPresidentUserId)
               .eq(ClubMember::getStatus, MemberStatus.ACTIVE.name());
        ClubMember newPresidentMember = clubMemberService.getOne(wrapper);
        if (newPresidentMember == null) throw new BusinessException("新社长必须是社团成员");
        // 原社长变为普通成员
        LambdaQueryWrapper<ClubMember> oldWrapper = new LambdaQueryWrapper<>();
        oldWrapper.eq(ClubMember::getClubId, clubId)
                  .eq(ClubMember::getMemberRole, MemberRole.PRESIDENT.name())
                  .eq(ClubMember::getStatus, MemberStatus.ACTIVE.name());
        ClubMember oldPresident = clubMemberService.getOne(oldWrapper);
        if (oldPresident != null) {
            oldPresident.setMemberRole(MemberRole.MEMBER.name());
            clubMemberService.updateById(oldPresident);
        }
        // 新社长
        newPresidentMember.setMemberRole(MemberRole.PRESIDENT.name());
        clubMemberService.updateById(newPresidentMember);
        club.setPresidentId(newPresidentUserId);
        updateById(club);
    }

    @Override
    public Club getByUserId(Long userId) {
        LambdaQueryWrapper<ClubMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ClubMember::getUserId, userId)
                     .eq(ClubMember::getStatus, MemberStatus.ACTIVE.name());
        ClubMember member = clubMemberService.getOne(memberWrapper);
        if (member == null) return null;
        return getById(member.getClubId());
    }

    private void validateTransition(String currentStatus, String targetStatus) {
        Set<String> allowed = VALID_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(targetStatus)) {
            throw new BusinessException("当前状态不允许该操作");
        }
    }
}
