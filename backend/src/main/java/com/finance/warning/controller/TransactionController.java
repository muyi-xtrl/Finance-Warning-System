package com.finance.warning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.warning.common.PageResult;
import com.finance.warning.common.Result;
import com.finance.warning.entity.FinanceTransaction;
import com.finance.warning.mapper.FinanceTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final FinanceTransactionMapper transactionMapper;

    @GetMapping("/page")
    public Result<PageResult<FinanceTransaction>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String entCode,
            @RequestParam(required = false) String txnType) {
        LambdaQueryWrapper<FinanceTransaction> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(entCode)) {
            qw.eq(FinanceTransaction::getEntCode, entCode);
        }
        if (StringUtils.hasText(txnType)) {
            qw.eq(FinanceTransaction::getTxnType, txnType);
        }
        qw.orderByDesc(FinanceTransaction::getTxnDate);
        Page<FinanceTransaction> p = transactionMapper.selectPage(new Page<>(page, size), qw);
        return Result.ok(new PageResult<>(p.getTotal(), page, size, p.getRecords()));
    }
}
