package com.finance.warning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.warning.common.PageResult;
import com.finance.warning.common.Result;
import com.finance.warning.entity.EnterpriseRiskScore;
import com.finance.warning.entity.WarningRecord;
import com.finance.warning.mapper.EnterpriseMapper;
import com.finance.warning.mapper.EnterpriseRiskScoreMapper;
import com.finance.warning.mapper.FinanceTransactionMapper;
import com.finance.warning.mapper.WarningRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final EnterpriseMapper enterpriseMapper;
    private final FinanceTransactionMapper transactionMapper;
    private final WarningRecordMapper warningRecordMapper;
    private final EnterpriseRiskScoreMapper riskScoreMapper;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("enterpriseCount", enterpriseMapper.selectCount(null));
        data.put("transactionCount", transactionMapper.selectCount(null));
        data.put("openWarningCount", warningRecordMapper.selectCount(
                new LambdaQueryWrapper<WarningRecord>().eq(WarningRecord::getStatus, "OPEN")));
        data.put("criticalCount", warningRecordMapper.selectCount(
                new LambdaQueryWrapper<WarningRecord>().eq(WarningRecord::getWarningLevel, "CRITICAL")));
        data.put("byLevel", warningRecordMapper.countByLevel());
        data.put("byStatus", warningRecordMapper.countByStatus());
        data.put("trend", warningRecordMapper.countTrend30Days());
        data.put("loanByIndustry", riskScoreMapper.loanByIndustry());
        data.put("topRisk", riskScoreMapper.selectList(
                new LambdaQueryWrapper<EnterpriseRiskScore>()
                        .orderByDesc(EnterpriseRiskScore::getRiskScore)
                        .last("LIMIT 10")));
        return Result.ok(data);
    }

    @GetMapping("/risk/page")
    public Result<PageResult<EnterpriseRiskScore>> riskPage(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String batch) {
        LambdaQueryWrapper<EnterpriseRiskScore> qw = new LambdaQueryWrapper<>();
        if (batch != null && !batch.isEmpty()) {
            qw.eq(EnterpriseRiskScore::getCalcBatch, batch);
        } else {
            // 取最新批次
            List<EnterpriseRiskScore> latest = riskScoreMapper.selectList(
                    new LambdaQueryWrapper<EnterpriseRiskScore>()
                            .orderByDesc(EnterpriseRiskScore::getCalcTime).last("LIMIT 1"));
            if (!latest.isEmpty()) {
                qw.eq(EnterpriseRiskScore::getCalcBatch, latest.get(0).getCalcBatch());
            }
        }
        qw.orderByDesc(EnterpriseRiskScore::getRiskScore);
        Page<EnterpriseRiskScore> p = riskScoreMapper.selectPage(new Page<>(page, size), qw);
        return Result.ok(new PageResult<>(p.getTotal(), page, size, p.getRecords()));
    }
}
