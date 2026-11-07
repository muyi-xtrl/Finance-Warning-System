package com.finance.warning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("enterprise")
public class Enterprise {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String entCode;
    private String entName;
    private String industry;
    private String region;
    private String creditLevel;
    private BigDecimal registeredCap;
    private Integer status;
    private LocalDateTime createTime;
}
