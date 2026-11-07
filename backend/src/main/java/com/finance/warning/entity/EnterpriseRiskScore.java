package com.finance.warning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("enterprise_risk_score")
public class EnterpriseRiskScore {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String entCode;
    private String entName;
    private BigDecimal totalLoan;
    private BigDecimal totalBalance;
    private Integer maxOverdue;
    private Integer overdueCount;
    private BigDecimal riskScore;
    private String riskLevel;
    private String calcBatch;
    private LocalDateTime calcTime;
}
