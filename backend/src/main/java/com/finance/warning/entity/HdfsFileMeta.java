package com.finance.warning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hdfs_file_meta")
public class HdfsFileMeta {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileName;
    private String hdfsPath;
    private Long fileSize;
    private String fileType;
    private String remark;
    private LocalDateTime uploadTime;
}
