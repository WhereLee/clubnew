package com.club;

import com.club.common.BusinessException;
import com.club.config.TestConfig;
import com.club.domain.Fund;
import com.club.enums.FundType;
import com.club.service.FundService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class LikeIdempotentTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private RedissonClient redissonClient;

    @Autowired
    private FundService fundService;

    @Test
    void fundApprovalIdempotent() {
        // 经费审批幂等：PENDING之外的状态不能再审批
        Fund fund = new Fund();
        fund.setClubId(5L);
        fund.setTitle("幂等测试");
        fund.setAmount(new BigDecimal("100"));
        fund.setType(FundType.INCOME.name());
        fund.setApplyUserId(1L);
        fundService.applyFund(fund);

        fundService.auditFund(fund.getId(), true, "通过", 1L);
        // 重复审批应抛异常
        assertThrows(BusinessException.class,
                () -> fundService.auditFund(fund.getId(), true, "再次通过", 1L));
    }
}
