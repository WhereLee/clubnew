package com.club.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.club.annotation.Log;
import com.club.common.R;
import com.club.domain.SysConfig;
import com.club.service.SysConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * 参数配置接口。
 * 缓存一致性由 {@link SysConfigService} 内部保证（写操作后自动失效缓存），
 * Controller 不感知缓存实现。
 */
@RestController
@RequestMapping("/system/config")
public class SysConfigController {

    @Resource
    private SysConfigService configService;

    @GetMapping("/list")
    public R<IPage<SysConfig>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String configName, String configKey) {
        return R.success(configService.listPage(pageNum, pageSize, configName, configKey));
    }

    @GetMapping("/{id}")
    public R<SysConfig> getById(@PathVariable Long id) {
        return R.success(configService.getById(id));
    }

    @PostMapping
    @Log(title = "参数配置", businessType = 1)
    public R<Void> add(@RequestBody SysConfig config) {
        configService.save(config);
        return R.success();
    }

    @PutMapping
    @Log(title = "参数配置", businessType = 2)
    public R<Void> update(@RequestBody SysConfig config) {
        configService.updateById(config);
        return R.success();
    }

    @DeleteMapping("/{ids}")
    @Log(title = "参数配置", businessType = 3)
    public R<Void> delete(@PathVariable String ids) {
        for (String id : Arrays.asList(ids.split(","))) {
            configService.removeById(Long.parseLong(id));
        }
        return R.success();
    }

    @GetMapping("/configKey/{key}")
    public R<String> getConfigByKey(@PathVariable String key) {
        return R.success(configService.getConfigValue(key));
    }
}
