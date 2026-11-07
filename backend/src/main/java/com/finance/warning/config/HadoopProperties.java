package com.finance.warning.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Hadoop 相关配置属性
 * 对应 application.yml 中的 hadoop.* 配置项
 */
@Component
@Data
@ConfigurationProperties(prefix = "hadoop")
public class HadoopProperties {

    /**
     * HDFS 地址
     */
    private String fsDefaultFS = "hdfs://hadoop100:8020";

    /**
     * YARN ResourceManager 地址
     */
    private String yarnRm = "hadoop101:8032";

    /**
     * 访问 HDFS 的用户名，必须与 hdfs-site.xml / core-site.xml 中一致
     */
    private String userName = "root";

    /**
     * 上传到 HDFS 的 jar 包路径
     * 需要把 jar 包放在服务器指定路径，路径可自定义
     */
    private String mrJar = "/opt/finance/hadoop-job/finance-warning-mr.jar";

    /**
     * MR 主类全限定名
     */
    private String mrMain = "com.finance.warning.mr.WarningJob";

    /**
     * HDFS 上存储原始数据的路径
     */
    private String rawPath = "/finance/raw";

    /**
     * HDFS 上存储计算结果的路径
     */
    private String resultPath = "/finance/result";

    /**
     * 是否启用 mock 模式（默认路径使用方式）
     */
    private boolean mockEnabled = true;
}