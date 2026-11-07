package com.finance.warning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("finance_transaction")
public class FinanceTransaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String txnId;
    private String entCode;
    private LocalDate txnDate;
    private String txnType;
    private BigDecimal amount;
    private BigDecimal balance;
    private Integer overdueDays;
    private BigDecimal interestRate;
    private Integer riskFlag;
    private LocalDateTime createTime;
}
