package com.club.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.club.annotation.Log;
import com.club.common.BusinessException;
import com.club.common.R;
import com.club.domain.SysDictData;
import com.club.domain.SysDictType;
import com.club.service.SysDictDataService;
import com.club.service.SysDictTypeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/system/dict/type")
public class SysDictTypeController {

    @Resource
    private SysDictTypeService dictTypeService;

    @Resource
    private SysDictDataService dictDataService;

    @GetMapping("/list")
    public R<IPage<SysDictType>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String dictName) {
        return R.success(dictTypeService.listPage(pageNum, pageSize, dictName));
    }

    @GetMapping("/{id}")
    public R<SysDictType> getById(@PathVariable Long id) {
        return R.success(dictTypeService.getById(id));
    }

    @PostMapping
    @Log(title = "字典类型", businessType = 1)
    public R<Void> add(@RequestBody SysDictType dictType) {
        dictTypeService.save(dictType);
        return R.success();
    }

    @PutMapping
    @Log(title = "字典类型", businessType = 2)
    public R<Void> update(@RequestBody SysDictType dictType) {
        dictTypeService.updateById(dictType);
        return R.success();
    }

    @DeleteMapping("/{ids}")
    @Log(title = "字典类型", businessType = 3)
    public R<Void> delete(@PathVariable String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        for (String id : idList) {
            SysDictType type = dictTypeService.getById(Long.parseLong(id));
            if (type != null) {
                // 级联删除该类型下的字典数据（Service 层会自动失效缓存），再删除类型本身
                LambdaQueryWrapper<SysDictData> dataWrapper = new LambdaQueryWrapper<>();
                dataWrapper.eq(SysDictData::getDictType, type.getDictType());
                dictDataService.remove(dataWrapper);
                dictTypeService.removeById(type.getId());
            }
        }
        return R.success();
    }

    @GetMapping("/optionselect")
    public R<List<SysDictType>> optionselect() {
        return R.success(dictTypeService.list());
    }
}
