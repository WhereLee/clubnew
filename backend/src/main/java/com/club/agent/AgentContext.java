package com.club.agent;

/**
 * Agent 调用上下文：当前请求用户的身份快照。
 * 工具通过它做权限判断与数据范围过滤（复用项目 RBAC/DataScope 语义）。
 *
 * @param userId    当前用户 ID
 * @param username  登录名
 * @param nickname  昵称
 * @param userType  STUDENT/ADMIN
 * @param dataScope 数据权限范围（1全部/2本社团及以下/3本社团/4仅本人/5自定义）
 * @param clubId    用户所属社团 ID（非社团成员为 null）
 */
public record AgentContext(
        Long userId,
        String username,
        String nickname,
        String userType,
        Integer dataScope,
        Long clubId) {

    /** 是否管理员 */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(userType) || Integer.valueOf(1).equals(dataScope);
    }

    /** 是否社团管理者（社长/副社长）：持有「本社团」级数据范围且确实属于某社团。
     *  判据必须双条件：dataScope ∈ {2 本社团及以下, 3 本社团, 5 自定义}（1=管理员走 isAdmin 分支，4=仅本人是普通学生）
     *  且 clubId 非空——单看 dataScope 会把普通学生（4 也 >= 2）误放行为社团管理者。 */
    public boolean isClubAdmin() {
        return clubId != null && dataScope != null
                && (dataScope == 2 || dataScope == 3 || dataScope == 5);
    }
}
