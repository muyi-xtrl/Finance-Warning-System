package com.finance.warning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("warning_record")
public class WarningRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String entCode;
    private String entName;
    private String ruleCode;
    private String warningLevel;
    private BigDecimal riskScore;
    private String title;
    private String content;
    private String source;
    private String status;
    private LocalDateTime warningTime;
    private String handleRemark;
    private LocalDateTime handleTime;
}
