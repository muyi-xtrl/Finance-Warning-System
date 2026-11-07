package com.finance.warning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("warning_rule")
public class WarningRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleCode;
    private String ruleName;
    private String ruleType;
    private BigDecimal thresholdValue;
    private String level;
    private String description;
    private Integer enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
