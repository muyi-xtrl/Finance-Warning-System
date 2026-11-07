package com.finance.warning.hadoop;

import com.finance.warning.config.HadoopProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HdfsService {

    private final HadoopProperties hadoopProperties;

    /**
     * 指定集群的用户配置
     */
    public Configuration buildConf() {
        if (hadoopProperties.getUserName() != null && !hadoopProperties.getUserName().isEmpty()) {
            System.setProperty("HADOOP_USER_NAME", hadoopProperties.getUserName());
        }
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", hadoopProperties.getFsDefaultFS());
        conf.set("fs.client.use.datanode.hostname", "true");
        conf.setBoolean("fs.hdfs.impl.disable.cache", true);
        return conf;
    }

    // ==================== 文件上传 ====================

    /**
     * 文件上传，让集群认清楚模拟路径和真实路径
     */
    public Map<String, Object> upload(MultipartFile file, String subDir) throws Exception {
        // 判断模拟路径是否可用
        if (hadoopProperties.isMockEnabled()) {
            Map<String, Object> mock = new HashMap<>();
            mock.put("hdfsPath",
                    hadoopProperties.getRawPath() + "/" + (subDir + "/") + file.getOriginalFilename());
            mock.put("fileSize", file.getSize());
            mock.put("mock", true);
            log.info("[MOCK] 模拟上传文件: {}", mock.get("hdfsPath"));
            return mock;
        }

        String target = hadoopProperties.getRawPath() + "/" + subDir + "/" + file.getOriginalFilename();
        FileSystem fs = FileSystem.get(buildConf());
        try {
            Path path = new Path(target);
            if (path.getParent() != null) {
                fs.mkdirs(path.getParent());
            }
            FSDataOutputStream out = fs.create(path, true);
            try {
                out.write(file.getBytes());
            } finally {
                out.close();
            }
            Map<String, Object> result = new HashMap<>();
            result.put("hdfsPath", target);
            result.put("fileSize", file.getSize());
            result.put("mock", false);
            log.info("文件已上传 HDFS: {} ({} bytes)", target, file.getSize());
            return result;
        } finally {
            fs.close();
        }
    }

    // ==================== 列出目录 ====================

    /**
     * 列出目录内容（Controller 里调用的方法名）
     */
    public List<Map<String, Object>> list(String path) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();

        if (hadoopProperties.isMockEnabled()) {
            log.info("[MOCK] 模拟列出目录: {}", path);
            return result;
        }

        FileSystem fs = FileSystem.get(buildConf());
        try {
            Path hdfsPath = new Path(path);
            if (!fs.exists(hdfsPath)) {
                return result;
            }
            FileStatus[] statuses = fs.listStatus(hdfsPath);
            for (FileStatus status : statuses) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", status.getPath().getName());
                map.put("path", status.getPath().toString());
                map.put("isDir", status.isDirectory());
                map.put("size", status.getLen());
                map.put("modifyTime", status.getModificationTime());
                result.add(map);
            }
        } finally {
            fs.close();
        }
        return result;
    }

    // ==================== 初始化基础目录 ====================

    /**
     * 确保基础目录存在（rawPath、resultPath）
     */
    public void ensureDirs() throws Exception {
        mkdirs(hadoopProperties.getRawPath());
        mkdirs(hadoopProperties.getResultPath());
    }

    // ==================== 创建目录 ====================

    /**
     * 创建目录
     */
    public boolean mkdirs(String path) throws Exception {
        if (hadoopProperties.isMockEnabled()) {
            log.info("[MOCK] 模拟创建目录: {}", path);
            return true;
        }
        FileSystem fs = FileSystem.get(buildConf());
        try {
            return fs.mkdirs(new Path(path));
        } finally {
            fs.close();
        }
    }

    // ==================== 判断存在 ====================

    /**
     * 判断路径是否存在
     */
    public boolean exists(String path) throws Exception {
        if (hadoopProperties.isMockEnabled()) {
            return true;
        }
        FileSystem fs = FileSystem.get(buildConf());
        try {
            return fs.exists(new Path(path));
        } finally {
            fs.close();
        }
    }

    // ==================== 删除 ====================

    /**
     * 删除文件或目录
     */
    public boolean delete(String path, boolean recursive) throws Exception {
        if (hadoopProperties.isMockEnabled()) {
            log.info("[MOCK] 模拟删除: {}", path);
            return true;
        }
        FileSystem fs = FileSystem.get(buildConf());
        try {
            return fs.delete(new Path(path), recursive);
        } finally {
            fs.close();
        }
    }

    // ==================== 下载 ====================

    /**
     * 下载文件，返回输入流（调用方负责 close）
     */
    public FSDataInputStream download(String path) throws Exception {
        if (hadoopProperties.isMockEnabled()) {
            throw new UnsupportedOperationException("[MOCK] 模式不支持下载");
        }
        FileSystem fs = FileSystem.get(buildConf());
        Path hdfsPath = new Path(path);
        if (!fs.exists(hdfsPath)) {
            fs.close();
            return null;
        }
        return fs.open(hdfsPath);
    }

    // ==================== 读取文本 ====================

    /**
     * 读取 HDFS 上的文本文件为字符串
     */
    public String readAsString(String path) throws Exception {
        if (hadoopProperties.isMockEnabled()) {
            return "";
        }
        FileSystem fs = FileSystem.get(buildConf());
        FSDataInputStream in = null;
        try {
            in = fs.open(new Path(path));
            byte[] buffer = new byte[in.available()];
            in.readFully(buffer);
            return new String(buffer, "UTF-8");
        } finally {
            if (in != null) {
                in.close();
            }
            fs.close();
        }
    }
}