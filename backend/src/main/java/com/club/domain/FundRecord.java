package com.club.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.club.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data @EqualsAndHashCode(callSuper = true) @TableName("fund_record")
public class FundRecord extends BaseEntity {
    private Long fundId; private Long clubId; private BigDecimal amount;
    private String type; private BigDecimal balanceAfter;
}
