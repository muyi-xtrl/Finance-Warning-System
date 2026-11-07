package com.finance.warning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.warning.common.PageResult;
import com.finance.warning.common.Result;
import com.finance.warning.entity.WarningRecord;
import com.finance.warning.mapper.WarningRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/warning")
@RequiredArgsConstructor
public class WarningRecordController {

    private final WarningRecordMapper warningRecordMapper;

    @GetMapping("/page")
    public Result<PageResult<WarningRecord>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<WarningRecord> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(level)) {
            qw.eq(WarningRecord::getWarningLevel, level);
        }
        if (StringUtils.hasText(status)) {
            qw.eq(WarningRecord::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(WarningRecord::getEntName, keyword).or().like(WarningRecord::getTitle, keyword));
        }
        qw.orderByDesc(WarningRecord::getWarningTime);
        Page<WarningRecord> p = warningRecordMapper.selectPage(new Page<>(page, size), qw);
        return Result.ok(new PageResult<>(p.getTotal(), page, size, p.getRecords()));
    }

    @PutMapping("/handle")
    public Result<Void> handle(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(String.valueOf(body.get("id")));
        String status = String.valueOf(body.get("status"));
        String remark = body.get("handleRemark") == null ? "" : String.valueOf(body.get("handleRemark"));
        WarningRecord record = warningRecordMapper.selectById(id);
        if (record != null) {
            record.setStatus(status);
            record.setHandleRemark(remark);
            record.setHandleTime(LocalDateTime.now());
            warningRecordMapper.updateById(record);
        }
        return Result.ok();
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> map = new HashMap<>();
        map.put("byLevel", warningRecordMapper.countByLevel());
        map.put("byStatus", warningRecordMapper.countByStatus());
        map.put("trend", warningRecordMapper.countTrend30Days());
        return Result.ok(map);
    }
}
