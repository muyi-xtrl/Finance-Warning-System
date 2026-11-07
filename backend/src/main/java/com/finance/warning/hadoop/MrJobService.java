package com.finance.warning.hadoop;

import com.finance.warning.config.HadoopProperties;
import com.finance.warning.entity.HadoopJobLog;
import com.finance.warning.mapper.HadoopJobLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * MapReduce 作业提交服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MrJobService {

    private final HadoopProperties proper;
    private final HdfsService hdfsService;
    private final HadoopJobLogMapper jobLogMapper;

    /**
     * 集群信息
     */
    public Map<String, Object> clusterInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("fsDefaultFS", proper.getFsDefaultFS());
        map.put("yarnRm", proper.getYarnRm());
        map.put("userName", proper.getUserName());
        map.put("mockEnabled", proper.isMockEnabled());
        map.put("mrJar", proper.getMrJar());
        map.put("mrMain", proper.getMrMain());
        return map;
    }

    /**
     * 提交风险预警 MR 作业
     *
     * @param inputPath 输入路径（HDFS），为 null 时用默认 rawPath
     * @return 作业日志
     */
    public HadoopJobLog submitRiskJob(String inputPath) {
        HadoopJobLog jobLog = new HadoopJobLog();
        jobLog.setJobName("RiskWarningJob");
        jobLog.setStartTime(LocalDateTime.now());
        jobLog.setStatus("RUNNING");

        // 默认输入
        if (inputPath == null || inputPath.isEmpty()) {
            inputPath = proper.getRawPath();
        }
        jobLog.setInputPath(inputPath);

        // 输出路径：加时间戳，避免目录已存在
        String batchNo = "BATCH_" + System.currentTimeMillis();
        String outputPath = proper.getResultPath() + "/" + batchNo;
        jobLog.setOutputPath(outputPath);
        jobLog.setBatchNo(batchNo);

        try {
            boolean ok;
            if (proper.isMockEnabled()) {
                log.info("[MOCK] 模拟提交 MR 作业: {} -> {}", inputPath, outputPath);
                Thread.sleep(500);   // 模拟耗时
                ok = true;
            } else {
                ok = submitToYarn(inputPath, outputPath);
            }

            jobLog.setStatus(ok ? "SUCCESS" : "FAILED");
            jobLog.setMessage(ok ? "作业执行成功" : "作业执行失败");   // ✅ 用 message
            jobLog.setEndTime(LocalDateTime.now());
        } catch (Exception e) {
            log.error("提交 MR 失败", e);
            jobLog.setStatus("FAILED");
            jobLog.setMessage(e.getMessage());   // ✅ 改成 setMessage
            jobLog.setEndTime(LocalDateTime.now());
        }

        // 入库
        try {
            jobLogMapper.insert(jobLog);
        } catch (Exception e) {
            log.warn("写入作业日志失败", e);
        }
        return jobLog;
    }

    /**
     * 真正提交到 YARN
     */
    private boolean submitToYarn(String inputPath, String outputPath) throws Exception {
        Configuration conf = hdfsService.buildConf();
        conf.set("mapreduce.framework.name", "yarn");
        conf.set("yarn.resourcemanager.address", proper.getYarnRm());
        conf.set("mapreduce.app-submission.cross-platform", "true");
        conf.set("mapreduce.job.jar", proper.getMrJar());

        // 反射调用 MR 主类，要求它有一个静态方法：
        // public static boolean run(Configuration conf, String input, String output)
        Class<?> clazz = Class.forName(proper.getMrMain());
        java.lang.reflect.Method runMethod = clazz.getMethod(
                "run", Configuration.class, String.class, String.class);
        Object result = runMethod.invoke(null, conf, inputPath, outputPath);

        // 同时设置 Job 的 jar（部分场景下反射不够）
        Job job = Job.getInstance(conf, "risk warning");
        job.setJarByClass(clazz);

        return Boolean.TRUE.equals(result);
    }

    /**
     * 从 HDFS 读取 MR 结果，同步写入 MySQL
     *
     * @param resultPath 结果文件路径（如 /finance/result/BATCH_xxx/part-r-00000）
     * @param batchNo    批次号
     */
    public void syncResultFromHdfs(String resultPath, String batchNo) throws Exception {
        if (proper.isMockEnabled()) {
            log.info("[MOCK] 模拟同步结果: {} ({})", resultPath, batchNo);
            return;
        }

        Configuration conf = hdfsService.buildConf();
        FileSystem fs = FileSystem.get(conf);
        try {
            Path path = new Path(resultPath);
            if (!fs.exists(path)) {
                log.warn("结果文件不存在: {}", resultPath);
                return;
            }

            try (FSDataInputStream in = fs.open(path);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {

                String line;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue ;

                    // 结果格式：key \t value
                    String[] parts = line.split("\\s+");
                    if (parts.length < 2) continue;

                    // TODO：把结果写入你的业务表
                    // 例如：warningResultMapper.insert(new WarningResult(batchNo, parts[0], parts[1]));
                    log.info("结果: batchNo={}, key={}, value={}", batchNo, parts[0], parts[1]);
                    count++;
                }
                log.info("同步完成，共 {} 行", count);
            }
        } finally {
            fs.close();
        }
    }
}