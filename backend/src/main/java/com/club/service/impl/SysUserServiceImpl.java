package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.BusinessException;
import com.club.domain.SysUser;
import com.club.mapper.SysUserMapper;
import com.club.service.SysUserService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public IPage<SysUser> listPage(Integer pageNum, Integer pageSize, String userName, String phone, String status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(userName)) {
            wrapper.like(SysUser::getUsername, userName);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(SysUser::getPhone, phone);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByAsc(SysUser::getId);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    @Transactional
    public boolean addUser(SysUser user) {
        // 检查用户名唯一
        SysUser existing = getByUsername(user.getUsername());
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return save(user);
    }

    @Override
    public boolean resetPassword(Long userId, String password) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(password));
        return updateById(user);
    }

    public SysUserMapper getBaseMapper() {
        return sysUserMapper;
    }
}
