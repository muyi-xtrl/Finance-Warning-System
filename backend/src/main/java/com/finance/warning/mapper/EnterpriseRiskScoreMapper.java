package com.finance.warning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.warning.entity.EnterpriseRiskScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface EnterpriseRiskScoreMapper extends BaseMapper<EnterpriseRiskScore> {

    @Select("SELECT e.industry AS name, COALESCE(SUM(s.total_loan),0) AS value " +
            "FROM enterprise e LEFT JOIN enterprise_risk_score s ON e.ent_code = s.ent_code " +
            "AND s.calc_batch = (SELECT calc_batch FROM enterprise_risk_score ORDER BY calc_time DESC LIMIT 1) " +
            "GROUP BY e.industry")
    List<Map<String, Object>> loanByIndustry();
}
