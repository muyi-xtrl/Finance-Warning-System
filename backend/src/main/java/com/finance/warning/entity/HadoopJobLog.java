package com.finance.warning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hadoop_job_log")
public class HadoopJobLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String jobName;
    private String jobId;
    private String batchNo;
    private String inputPath;
    private String outputPath;
    private String status;
    private String message;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
