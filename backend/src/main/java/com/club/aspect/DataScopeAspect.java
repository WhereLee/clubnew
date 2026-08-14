package com.club.aspect;

import com.club.annotation.DataScope;
import com.club.security.LoginUser;
import com.club.security.SecurityUtils;
import jakarta.annotation.Resource;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据权限切面。
 * <p>
 * 作用：在标注了 {@code @DataScope} 的查询方法执行前，根据当前登录用户角色的
 * {@code data_scope} 字段生成数据过滤 SQL 片段，写入 ThreadLocal；
 * 由 Service/Mapper 层在真正拼查询时消费（通过 {@link #getDataScope()} 读取），
 * 方法执行结束后通过 {@code @After} 清理 ThreadLocal，避免线程池复用导致数据串味。
 * </p>
 */
@Aspect
@Component
public class DataScopeAspect {

    private static final Logger log = LoggerFactory.getLogger(DataScopeAspect.class);

    /** 全部数据权限 */
    public static final String SCOPE_ALL = "1";
    /** 本社团及以下 */
    public static final String SCOPE_DEPT_AND_BELOW = "2";
    /** 本社团 */
    public static final String SCOPE_DEPT = "3";
    /** 仅本人 */
    public static final String SCOPE_SELF = "4";
    /** 自定义 */
    public static final String SCOPE_CUSTOM = "5";

    private static final ThreadLocal<String> DATA_SCOPE_HOLDER = new ThreadLocal<>();

    @Resource
    private JdbcTemplate jdbcTemplate;

    /** 供 Service/Mapper 层读取当前线程的数据权限过滤片段 */
    public static String getDataScope() {
        return DATA_SCOPE_HOLDER.get();
    }

    /** 清理 ThreadLocal，防止线程池复用导致数据串味 */
    public static void clearAfterMapper() {
        DATA_SCOPE_HOLDER.remove();
    }

    @Before("@annotation(dataScope)")
    public void doBefore(DataScope dataScope) {
        try {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser == null) {
                return;
            }
            // 管理员拥有全部数据权限，不加过滤
            if ("ADMIN".equals(loginUser.getUserType())) {
                return;
            }

            String userAlias = dataScope.userAlias();

            // 查询当前用户所有角色的 data_scope
            List<String> scopes = jdbcTemplate.queryForList(
                    "SELECT r.data_scope FROM sys_role r " +
                    "INNER JOIN sys_user_role ur ON ur.role_id = r.id " +
                    "WHERE ur.user_id = ? AND r.status = '0' AND r.deleted = 0",
                    String.class, loginUser.getUserId());

            if (scopes == null || scopes.isEmpty()) {
                return;
            }

            // 多角色时取「最严格」的数据范围（权限最小）：仅本人(4) > 自定义(5) > 本社团(3) > 本社团及以下(2) > 全部(1)
            String strictest = scopes.stream().max(this::compareStrictness).orElse(SCOPE_SELF);

            StringBuilder sql = new StringBuilder();
            switch (strictest) {
                case SCOPE_ALL:
                    // 全部数据，不加过滤
                    break;
                case SCOPE_DEPT_AND_BELOW:
                case SCOPE_DEPT:
                case SCOPE_CUSTOM:
                    // 本社团：只能看到自己所属社团的数据
                    sql.append(userAlias).append(".id IN (SELECT cm.club_id FROM club_member cm ")
                       .append("WHERE cm.user_id = ").append(loginUser.getUserId())
                       .append(" AND cm.status = 'ACTIVE' AND cm.deleted = 0)");
                    break;
                case SCOPE_SELF:
                    // 仅本人创建的数据
                    sql.append(userAlias).append(".create_user_id = ").append(loginUser.getUserId());
                    break;
                default:
                    break;
            }

            if (sql.length() > 0) {
                DATA_SCOPE_HOLDER.set(sql.toString());
            }
        } catch (Exception e) {
            // 数据权限异常不应阻断业务，但记录日志便于排查
            log.warn("生成数据权限过滤片段失败: {}", e.getMessage());
        }
    }

    /** 标注了 @DataScope 的方法执行结束后清理 ThreadLocal */
    @After("@annotation(com.club.annotation.DataScope)")
    public void doAfter() {
        clearAfterMapper();
    }

    /** 严格程度比较：数值越大越严格 */
    private int compareStrictness(String a, String b) {
        return Integer.compare(strictness(a), strictness(b));
    }

    private int strictness(String scope) {
        if (scope == null) {
            return -1;
        }
        switch (scope) {
            case SCOPE_ALL:             return 0;
            case SCOPE_DEPT_AND_BELOW:  return 1;
            case SCOPE_DEPT:            return 2;
            case SCOPE_CUSTOM:          return 3;
            case SCOPE_SELF:            return 4;
            default:                    return -1;
        }
    }
}
