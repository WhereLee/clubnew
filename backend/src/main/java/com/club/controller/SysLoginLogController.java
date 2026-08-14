package com.club.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.club.common.R;
import com.club.domain.SysLoginLog;
import com.club.service.SysLoginLogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/system/loginlog")
public class SysLoginLogController {

    @Resource
    private SysLoginLogService loginLogService;

    @GetMapping("/list")
    public R<IPage<SysLoginLog>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String userName, String status) {
        return R.success(loginLogService.listPage(pageNum, pageSize, userName, status));
    }

    @DeleteMapping("/{ids}")
    public R<Void> delete(@PathVariable String ids) {
        loginLogService.removeByIds(Arrays.asList(ids.split(",")));
        return R.success();
    }
}
