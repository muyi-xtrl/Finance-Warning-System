package com.finance.warning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.warning.common.PageResult;
import com.finance.warning.common.Result;
import com.finance.warning.config.HadoopProperties;
import com.finance.warning.entity.HadoopJobLog;
import com.finance.warning.entity.HdfsFileMeta;
import com.finance.warning.hadoop.HdfsService;
import com.finance.warning.hadoop.MrJobService;
import com.finance.warning.mapper.HadoopJobLogMapper;
import com.finance.warning.mapper.HdfsFileMetaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hadoop 相关接口：
 *  - 集群信息
 *  - HDFS 上传 / 列表 / 初始化目录
 *  - MR 作业提交 / 日志查询 / 结果同步
 */
@RestController
@RequestMapping("/hadoop")
@RequiredArgsConstructor
@Slf4j
public class HadoopController {

    private final HdfsService hdfsService;
    private final MrJobService mrJobService;
    private final HadoopJobLogMapper jobLogMapper;
    private final HdfsFileMetaMapper fileMetaMapper;
    private final HadoopProperties hadoopProperties;

    /**
     * 集群信息
     * GET /api/hadoop/cluster
     */
    @GetMapping("/cluster")
    public Result<Map<String, Object>> cluster() {
        return Result.ok(mrJobService.clusterInfo());
    }

    /**
     * 上传文件到 HDFS
     * POST /api/hadoop/hdfs/upload?subDir=xxx
     */
    @PostMapping("/hdfs/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(required = false) String subDir) throws Exception {
        Map<String, Object> uploaded = hdfsService.upload(file, subDir);

        HdfsFileMeta meta = new HdfsFileMeta();
        meta.setFileName(file.getOriginalFilename());
        meta.setHdfsPath(String.valueOf(uploaded.get("hdfsPath")));
        meta.setFileSize(file.getSize());
        meta.setFileType("RAW");
        meta.setRemark("通过平台上传");
        fileMetaMapper.insert(meta);
        uploaded.put("metaId", meta.getId());

        return Result.ok(uploaded);
    }

    /**
     * 列出 HDFS 目录
     * GET /api/hadoop/hdfs/list?path=/finance/raw
     */
    @GetMapping("/hdfs/list")
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false) String path) throws Exception {
        String p = (path == null || path.isEmpty()) ? hadoopProperties.getRawPath() : path;
        return Result.ok(hdfsService.list(p));
    }

    /**
     * 分页查询已上传的文件元数据
     * GET /api/hadoop/hdfs/files?page=1&size=10
     */
    @GetMapping("/hdfs/files")
    public Result<PageResult<HdfsFileMeta>> files(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Page<HdfsFileMeta> p = fileMetaMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<HdfsFileMeta>()
                        .orderByDesc(HdfsFileMeta::getUploadTime));
        return Result.ok(new PageResult<>(p.getTotal(), page, size, p.getRecords()));
    }

    /**
     * 初始化 HDFS 目录（rawPath / resultPath）
     * POST /api/hadoop/hdfs/init
     */
    @PostMapping("/hdfs/init")
    public Result<Map<String, Object>> initDirs() throws Exception {
        hdfsService.ensureDirs();
        Map<String, Object> map = new HashMap<>();
        map.put("rawPath", hadoopProperties.getRawPath());
        map.put("resultPath", hadoopProperties.getResultPath());
        return Result.ok(map);
    }

    /**
     * 提交风险预警 MR 作业
     * POST /api/hadoop/mr/submit
     * body: { "inputPath": "/finance/raw" }
     */
    @PostMapping("/mr/submit")
    public Result<HadoopJobLog> submit(@RequestBody(required = false) Map<String, String> body) {
        String input = (body == null) ? null : body.get("inputPath");
        return Result.ok(mrJobService.submitRiskJob(input));
    }

    /**
     * 分页查询 MR 作业日志
     * GET /api/hadoop/mr/logs?page=1&size=10
     */
    @GetMapping("/mr/logs")
    public Result<PageResult<HadoopJobLog>> logs(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Page<HadoopJobLog> p = jobLogMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<HadoopJobLog>()
                        .orderByDesc(HadoopJobLog::getStartTime));
        return Result.ok(new PageResult<>(p.getTotal(), page, size, p.getRecords()));
    }

    /**
     * 在集群上手动跑完 MR 后，把结果文件回写 MySQL
     * POST /api/hadoop/mr/sync
     * body: { "resultPath": "/finance/result/BATCH_xxx/part-r-00000", "batchNo": "BATCH_xxx" }
     */
    @PostMapping("/mr/sync")
    public Result<Void> syncResult(@RequestBody Map<String, String> body) throws Exception {
        String resultPath = body.get("resultPath");
        String batchNo = body.get("batchNo");

        if (resultPath == null || resultPath.isEmpty()) {
            resultPath = hadoopProperties.getResultPath() + "/" + batchNo + "/part-r-00000";
        }

        mrJobService.syncResultFromHdfs(resultPath, batchNo);
        return Result.ok();
    }
}