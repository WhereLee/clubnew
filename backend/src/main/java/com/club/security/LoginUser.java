package com.club.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户信息（Spring Security UserDetails）
 */
@Data
public class LoginUser implements UserDetails {

    private Long userId;
    private String username;
    @JsonIgnore
    private String password;
    private String nickname;
    private String userType;
    private Set<String> permissions;

    public LoginUser() {}

    public LoginUser(Long userId, String username, String password, String nickname, String userType, Set<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.userType = userType;
        this.permissions = permissions;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (permissions == null) return Set.of();
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return username;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
