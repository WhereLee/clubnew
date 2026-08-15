package com.club.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.club.common.R;
import com.club.domain.SysOperLog;
import com.club.service.SysOperLogService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/system/operlog")
public class SysOperLogController {

    @Resource
    private SysOperLogService operLogService;

    @PreAuthorize("@ss.hasPermi('monitor:operlog:list')")
    @GetMapping("/list")
    public R<IPage<SysOperLog>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String title, String operName, Integer status) {
        return R.success(operLogService.listPage(pageNum, pageSize, title, operName, status));
    }

    @PreAuthorize("@ss.hasPermi('monitor:operlog:list')")
    @DeleteMapping("/{ids}")
    public R<Void> delete(@PathVariable String ids) {
        operLogService.removeByIds(Arrays.asList(ids.split(",")));
        return R.success();
    }
}
