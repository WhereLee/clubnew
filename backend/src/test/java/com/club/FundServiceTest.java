package com.club;

import com.club.config.TestConfig;
import com.club.domain.Fund;
import com.club.enums.FundType;
import com.club.service.FundService;
import org.junit.jupiter.api.Test;
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
class FundServiceTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private FundService fundService;

    @Test
    void amountZeroOrNegative_throwsException() {
        Fund fund = new Fund();
        fund.setClubId(1L);
        fund.setTitle("测试");
        fund.setAmount(BigDecimal.ZERO);
        fund.setType(FundType.INCOME.name());
        assertThrows(com.club.common.BusinessException.class, () -> fundService.applyFund(fund));

        fund.setAmount(new BigDecimal("-100"));
        assertThrows(com.club.common.BusinessException.class, () -> fundService.applyFund(fund));
    }

    @Test
    void auditApproved_generatesRecord_balanceCorrect() {
        Fund fund = new Fund();
        fund.setClubId(2L);
        fund.setTitle("测试收入");
        fund.setAmount(new BigDecimal("500"));
        fund.setType(FundType.INCOME.name());
        fund.setApplyUserId(1L);
        fundService.applyFund(fund);

        fundService.auditFund(fund.getId(), true, "通过", 1L);
        BigDecimal balance = fundService.getBalance(2L);
        assertEquals(0, new BigDecimal("500").compareTo(balance), "余额应为500");
    }

    @Test
    void duplicateAudit_throwsException() {
        Fund fund = new Fund();
        fund.setClubId(3L);
        fund.setTitle("重复审批测试");
        fund.setAmount(new BigDecimal("100"));
        fund.setType(FundType.EXPENSE.name());
        fund.setApplyUserId(1L);
        fundService.applyFund(fund);

        fundService.auditFund(fund.getId(), true, "通过", 1L);
        assertThrows(com.club.common.BusinessException.class,
                () -> fundService.auditFund(fund.getId(), true, "再次通过", 1L));
    }

    @Test
    void balanceIncomeMinusExpense() {
        // 收入1000
        Fund income = new Fund();
        income.setClubId(4L);
        income.setTitle("收入");
        income.setAmount(new BigDecimal("1000"));
        income.setType(FundType.INCOME.name());
        income.setApplyUserId(1L);
        fundService.applyFund(income);
        fundService.auditFund(income.getId(), true, "通过", 1L);

        // 支出300
        Fund expense = new Fund();
        expense.setClubId(4L);
        expense.setTitle("支出");
        expense.setAmount(new BigDecimal("300"));
        expense.setType(FundType.EXPENSE.name());
        expense.setApplyUserId(1L);
        fundService.applyFund(expense);
        fundService.auditFund(expense.getId(), true, "通过", 1L);

        BigDecimal balance = fundService.getBalance(4L);
        assertEquals(0, new BigDecimal("700").compareTo(balance), "余额应为700");
    }
}
