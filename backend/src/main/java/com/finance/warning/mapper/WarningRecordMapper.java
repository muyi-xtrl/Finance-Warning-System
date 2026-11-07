package com.finance.warning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.warning.entity.WarningRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface WarningRecordMapper extends BaseMapper<WarningRecord> {

    @Select("SELECT warning_level AS name, COUNT(*) AS value FROM warning_record GROUP BY warning_level")
    List<Map<String, Object>> countByLevel();

    @Select("SELECT status AS name, COUNT(*) AS value FROM warning_record GROUP BY status")
    List<Map<String, Object>> countByStatus();

    @Select("SELECT DATE_FORMAT(warning_time,'%Y-%m-%d') AS name, COUNT(*) AS value " +
            "FROM warning_record WHERE warning_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "GROUP BY DATE_FORMAT(warning_time,'%Y-%m-%d') ORDER BY name")
    List<Map<String, Object>> countTrend30Days();
}
