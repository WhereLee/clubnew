package com.club.service;

import com.club.vo.LoginVO;

/**
 * Refresh Token 服务：不透明随机串 + Redis 存储，支持轮换、复用检测与吊销。
 *
 * <p>安全模型（OAuth 2.0 Security BCP / RFC 8725 思路）：
 * <ul>
 *   <li>每个 refresh token 只能使用一次（轮换），使用后立即作废并签发新对；</li>
 *   <li>已轮换的旧 token 再次出现 = 疑似泄露（受害者正在正常轮换，攻击者在复用旧值）→
 *       吊销该用户全部 refresh 会话 + 拉黑 userId（access 剩余有效期内的请求全部拒绝）；</li>
 *   <li>Redis 不可用时 refresh 直接失败（fail-secure）——续期是低频操作，安全优先；
 *       与 access 认证的 fail-open 形成互补。</li>
 * </ul>
 */
public interface RefreshTokenService {

    /**
     * 为 userId 签发 refresh token 并登记会话（登录成功后调用）。
     */
    String issueRefreshToken(Long userId);

    /**
     * 用 refresh token 换取新的 access + refresh（轮换）。
     *
     * @throws com.club.common.BusinessException refresh 无效/已过期/疑似泄露
     */
    LoginVO refresh(String refreshToken);

    /**
     * 注销单个 refresh token（主动登出时调用）。
     */
    void revoke(String refreshToken);

    /**
     * 吊销该用户全部 refresh 会话（复用检测触发/管理端踢人）。
     */
    void revokeAll(Long userId);

    /**
     * 用户是否在拉黑名单中（复用检测触发，TTL = access 有效期）。
     */
    boolean isBlacklisted(Long userId);

    /**
     * 解除拉黑（用户通过密码重新认证成功后调用：密码验证通过即证明身份，
     * 拉黑仅针对「旧会话残余风险」，不应阻止用户正常重新登录）。
     */
    void clearBlacklist(Long userId);
}
