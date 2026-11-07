package com.finance.warning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.warning.common.PageResult;
import com.finance.warning.common.Result;
import com.finance.warning.entity.WarningRule;
import com.finance.warning.mapper.WarningRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rule")
@RequiredArgsConstructor
public class WarningRuleController {

    private final WarningRuleMapper ruleMapper;

    @GetMapping("/list")
    public Result<List<WarningRule>> list() {
        return Result.ok(ruleMapper.selectList(new LambdaQueryWrapper<WarningRule>().orderByAsc(WarningRule::getId)));
    }

    @GetMapping("/page")
    public Result<PageResult<WarningRule>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Page<WarningRule> p = ruleMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<WarningRule>().orderByAsc(WarningRule::getId));
        return Result.ok(new PageResult<>(p.getTotal(), page, size, p.getRecords()));
    }

    @PostMapping
    public Result<Void> create(@RequestBody WarningRule rule) {
        ruleMapper.insert(rule);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody WarningRule rule) {
        ruleMapper.updateById(rule);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ruleMapper.deleteById(id);
        return Result.ok();
    }
}
