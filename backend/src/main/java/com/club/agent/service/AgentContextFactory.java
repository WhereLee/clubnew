package com.club.agent.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.club.agent.AgentContext;
import com.club.common.BusinessException;
import com.club.common.ResultCode;
import com.club.security.LoginUser;
import com.club.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

/**
 * Agent 上下文工厂：从当前认证用户构造 AgentContext。
 *
 * dataScope 语义与 DataScopeAspect 同源：多角色取「最严格」范围
 * （严格序：仅本人(4) > 自定义(5) > 本社团(3) > 本社团及以下(2) > 全部(1)）。
 * clubId 取用户在社社团（club_member ACTIVE），非社团成员为 null。
 */
@Component
@RequiredArgsConstructor
public class AgentContextFactory {

    private final JdbcTemplate jdbcTemplate;

    public AgentContext build() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }

        Integer dataScope = null;
        Long clubId = null;
        try {
            List<String> scopes = jdbcTemplate.queryForList(
                    "SELECT r.data_scope FROM sys_role r " +
                    "INNER JOIN sys_user_role ur ON ur.role_id = r.id " +
                    "WHERE ur.user_id = ? AND r.status = '0' AND r.deleted = 0",
                    String.class, loginUser.getUserId());

            dataScope = scopes.stream()
                    .map(Integer::parseInt)
                    .max(this::compareStrictness)
                    .orElse(4);

            List<Long> clubIds = jdbcTemplate.queryForList(
                    "SELECT club_id FROM club_member WHERE user_id = ? AND status = 'ACTIVE' AND deleted = 0 LIMIT 1",
                    Long.class, loginUser.getUserId());
            clubId = clubIds.isEmpty() ? null : clubIds.get(0);
        } catch (Exception e) {
            // 上下文构造失败不阻断对话：降级为最小权限（仅本人）
            dataScope = 4;
        }

        return new AgentContext(loginUser.getUserId(), loginUser.getUsername(), loginUser.getNickname(),
                loginUser.getUserType(), dataScope, clubId);
    }

    /** 与 DataScopeAspect 相同的严格度比较：数值大者更严格，其中自定义(5) 位于仅本人(4) 之后 */
    private int compareStrictness(Integer a, Integer b) {
        return Integer.compare(strictnessRank(a), strictnessRank(b));
    }

    private int strictnessRank(Integer scope) {
        return switch (scope) {
            case 1 -> 1; // 全部
            case 2 -> 2; // 本社团及以下
            case 3 -> 3; // 本社团
            case 5 -> 4; // 自定义（按本社团处理）
            case 4 -> 5; // 仅本人（最严格）
            default -> 5;
        };
    }
}
