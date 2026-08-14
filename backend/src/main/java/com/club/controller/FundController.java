package com.club.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.club.annotation.Log;
import com.club.common.R;
import com.club.domain.Fund;
import com.club.domain.FundRecord;
import com.club.dto.FundAuditDTO;
import com.club.dto.FundDTO;
import com.club.security.SecurityUtils;
import com.club.service.FundService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/fund")
public class FundController {

    @Resource
    private FundService fundService;

    @GetMapping("/list")
    public R<IPage<Fund>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               Long clubId, String status) {
        return R.success(fundService.listPage(pageNum, pageSize, clubId, status));
    }

    @GetMapping("/{id}")
    public R<Fund> getById(@PathVariable Long id) {
        return R.success(fundService.getById(id));
    }

    @PostMapping
    @Log(title = "经费申请", businessType = 1)
    public R<Long> apply(@Valid @RequestBody FundDTO dto) {
        Fund fund = new Fund();
        fund.setClubId(dto.getClubId());
        fund.setTitle(dto.getTitle());
        fund.setAmount(dto.getAmount());
        fund.setType(dto.getType());
        fund.setApplyUserId(SecurityUtils.getUserId());
        return R.success(fundService.applyFund(fund));
    }

    @PutMapping("/{id}/audit")
    @PreAuthorize("@ss.hasPermi('fund:audit')")
    @Log(title = "经费审批", businessType = 2)
    public R<Void> audit(@PathVariable Long id, @Valid @RequestBody FundAuditDTO dto) {
        fundService.auditFund(id, dto.getApproved(), dto.getRemark(), SecurityUtils.getUserId());
        return R.success();
    }

    @GetMapping("/{clubId}/balance")
    public R<BigDecimal> balance(@PathVariable Long clubId) {
        return R.success(fundService.getBalance(clubId));
    }

    @GetMapping("/record/list")
    public R<IPage<FundRecord>> records(Long clubId,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.success(fundService.listRecords(clubId, pageNum, pageSize));
    }
}
