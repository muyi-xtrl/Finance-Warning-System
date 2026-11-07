package com.finance.warning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.warning.common.PageResult;
import com.finance.warning.common.Result;
import com.finance.warning.entity.Enterprise;
import com.finance.warning.mapper.EnterpriseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enterprise")
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseMapper enterpriseMapper;

    @GetMapping("/page")
    public Result<PageResult<Enterprise>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String industry) {
        LambdaQueryWrapper<Enterprise> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(Enterprise::getEntName, keyword).or().like(Enterprise::getEntCode, keyword));
        }
        if (StringUtils.hasText(industry)) {
            qw.eq(Enterprise::getIndustry, industry);
        }
        qw.orderByDesc(Enterprise::getId);
        Page<Enterprise> p = enterpriseMapper.selectPage(new Page<>(page, size), qw);
        return Result.ok(new PageResult<>(p.getTotal(), page, size, p.getRecords()));
    }

    @GetMapping("/{id}")
    public Result<Enterprise> detail(@PathVariable Long id) {
        return Result.ok(enterpriseMapper.selectById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Enterprise enterprise) {
        enterpriseMapper.insert(enterprise);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Enterprise enterprise) {
        enterpriseMapper.updateById(enterprise);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        enterpriseMapper.deleteById(id);
        return Result.ok();
    }
}
