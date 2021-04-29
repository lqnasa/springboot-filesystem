package com.coder.lee.filesystem.service.impl;


import com.coder.lee.filesystem.constant.Constants;
import com.coder.lee.filesystem.request.MultipartFileParam;
import com.coder.lee.filesystem.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;


/**
 * Description: StorageServiceImpl
 * Copyright: Copyright (c)
 * Company: Ruijie Co., Ltd.
 * Date:    2020/11/1 14:36
 *
 * @author coderLee23
 */
@Slf4j
@Service
public class StorageServiceImpl implements StorageService {

    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOperations;

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, Boolean> hashOperations;

    /**
     * 这个必须与前端设定的值一致
     */
    @Value("${breakpoint.upload.chunkSize}")
    private long chunkSize;

    /**
     * 保存文件的根目录
     */
    @Value("${breakpoint.upload.dir}")
    private String finalDirPath;


    @Override
    public void deleteAll() {
        log.info("开发初始化清理数据，start");
        FileSystemUtils.deleteRecursively(Paths.get(finalDirPath).toFile());
        // 不推荐这种删除，最好用scan，这里为了方便测试
        Set<String> keys = valueOperations.getOperations().keys(String.format("%s*", Constants.FILE_MD5_KEY));
        if (!CollectionUtils.isEmpty(keys)) {
            valueOperations.getOperations().delete(keys);
        }
        hashOperations.getOperations().delete(Constants.FILE_UPLOAD_STATUS);
        log.info("开发初始化清理数据，end");
    }


    @Override
    public void uploadFile(MultipartFileParam multipartFileParam) throws IOException {
        String fileName = multipartFileParam.getName();
        String uploadDirPath = finalDirPath + multipartFileParam.getMd5();
        String tempFileName = fileName + Constants.FILE_NAME_TMP_SUFFIX;
        File tmpDir = new File(uploadDirPath);
        File tmpFile = new File(uploadDirPath, tempFileName);
        if (!tmpDir.exists()) {
            boolean mkdirs = tmpDir.mkdirs();
            if (!mkdirs) {
                log.error("目录创建失败");
            }
        }

        File confFile = new File(uploadDirPath, multipartFileParam.getName() + Constants.FILE_NAME_CONF_SUFFIX);
        // 上传分块文件
        uploadChunkFileByRandomAccessFile(multipartFileParam, tmpFile);
        //标记上传进度
        tagChunkUpload(multipartFileParam, confFile);
        //检测是否全部上传完成
        boolean isComplete = isUploadComplete(confFile);
        if (isComplete) {
            //全部上传完成重命名文件
            renameFile(tmpFile, fileName);
        }
        // 更新上传结果
        updateUploadResult(multipartFileParam, isComplete);
        // 删除conf文件
        if (isComplete) {
            confFile.delete();
        }
    }

    private void uploadChunkFileByRandomAccessFile(MultipartFileParam multipartFileParam, File tmpFile) throws IOException {
        try (RandomAccessFile accessTmpFile = new RandomAccessFile(tmpFile, "rw")) {
            long offset = chunkSize * multipartFileParam.getChunk();
            // 定位到该分片的偏移量
            accessTmpFile.seek(offset);
            // 写入该分片数据
            accessTmpFile.write(multipartFileParam.getFile().getBytes());
        }
    }

    private void updateUploadResult(MultipartFileParam multipartFileParam, boolean isComplete) {
        log.info("isComplete ===>{}", isComplete);
        String md5 = multipartFileParam.getMd5();
        String name = multipartFileParam.getName();
        String fileMd5Key = Constants.FILE_MD5_KEY + md5;
        if (isComplete) {
            hashOperations.put(Constants.FILE_UPLOAD_STATUS, md5, true);
            valueOperations.set(fileMd5Key, finalDirPath + md5 + "/" + name);
        } else {
            if (!hashOperations.hasKey(Constants.FILE_UPLOAD_STATUS, md5)) {
                hashOperations.put(Constants.FILE_UPLOAD_STATUS, md5, false);
            }
            if (valueOperations.get(fileMd5Key) == null) {
                valueOperations.set(fileMd5Key, finalDirPath + md5 + "/" + name + Constants.FILE_NAME_CONF_SUFFIX);
            }
        }
    }


    private void tagChunkUpload(MultipartFileParam multipartFileParam, File confFile) throws IOException {
        try (RandomAccessFile accessConfFile = new RandomAccessFile(confFile, "rw")) {
            // 把该分段标记为 true 表示完成
            accessConfFile.setLength(multipartFileParam.getChunks());
            accessConfFile.seek(multipartFileParam.getChunk());
            accessConfFile.write(Byte.MAX_VALUE);
        }
    }

    private boolean isUploadComplete(File confFile) throws IOException {
        // completeList 检查是否全部完成,如果数组里是否全部都是(全部分片都成功上传)
        byte[] completeList = Files.readAllBytes(confFile.toPath());
        for (byte chunkComplete : completeList) {
            if (chunkComplete != Byte.MAX_VALUE) {
                return false;
            }
        }
        return true;
    }

    private boolean renameFile(File toBeRenamed, String toFileNewName) {
        // 检查要重命名的文件是否存在，是否是文件
        if (!toBeRenamed.exists() || toBeRenamed.isDirectory()) {
            log.info("File does not exist: {}", toBeRenamed.getName());
            return false;
        }
        File newFile = new File(toBeRenamed.getParent() + File.separatorChar + toFileNewName);
        // 修改文件名
        return toBeRenamed.renameTo(newFile);
    }

}
