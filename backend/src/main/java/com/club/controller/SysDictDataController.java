package com.club.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.club.annotation.Log;
import com.club.common.R;
import com.club.domain.SysDictData;
import com.club.service.SysDictDataService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 字典数据接口。
 * 缓存一致性由 {@link SysDictDataService} 内部保证（写操作后自动失效缓存），
 * Controller 不感知缓存实现。
 */
@RestController
@RequestMapping("/system/dict/data")
public class SysDictDataController {

    @Resource
    private SysDictDataService dictDataService;

    @GetMapping("/list")
    public R<IPage<SysDictData>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String dictType, String dictLabel) {
        return R.success(dictDataService.listPage(pageNum, pageSize, dictType, dictLabel));
    }

    @GetMapping("/{id}")
    public R<SysDictData> getById(@PathVariable Long id) {
        return R.success(dictDataService.getById(id));
    }

    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @PostMapping
    @Log(title = "字典数据", businessType = 1)
    public R<Void> add(@RequestBody SysDictData dictData) {
        dictDataService.save(dictData);
        return R.success();
    }

    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @PutMapping
    @Log(title = "字典数据", businessType = 2)
    public R<Void> update(@RequestBody SysDictData dictData) {
        dictDataService.updateById(dictData);
        return R.success();
    }

    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @DeleteMapping("/{ids}")
    @Log(title = "字典数据", businessType = 3)
    public R<Void> delete(@PathVariable String ids) {
        for (String id : Arrays.asList(ids.split(","))) {
            dictDataService.removeById(Long.parseLong(id));
        }
        return R.success();
    }

    @GetMapping("/type/{dictType}")
    public R<List<SysDictData>> getByType(@PathVariable String dictType) {
        return R.success(dictDataService.getByType(dictType));
    }
}
