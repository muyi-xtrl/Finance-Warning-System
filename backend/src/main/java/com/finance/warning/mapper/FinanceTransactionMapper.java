package com.finance.warning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.warning.entity.FinanceTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FinanceTransactionMapper extends BaseMapper<FinanceTransaction> {
}
