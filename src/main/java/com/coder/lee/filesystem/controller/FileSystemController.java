package com.coder.lee.filesystem.controller;

import com.coder.lee.filesystem.constant.Constants;
import com.coder.lee.filesystem.enums.ResultStatusEnum;
import com.coder.lee.filesystem.request.MultipartFileParam;
import com.coder.lee.filesystem.service.StorageService;
import com.coder.lee.filesystem.vo.FileUploadStatusVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Description: FileSystemController
 * Copyright: Copyright (c)
 * Company: Ruijie Co., Ltd.
 * Date:    2020/11/1 14:36
 *
 * @Author: coderLee23
 */
@Api
@RequestMapping("/filesystem")
@RestController
@Slf4j
public class FileSystemController {

    @Autowired
    private ValueOperations<String, Object> valueOperations;

    @Autowired
    private HashOperations<String, String, Object> hashOperations;

    @Autowired
    private StorageService storageService;

    /**
     * 秒传判断，断点判断
     *
     * @param md5 文件md5值
     * @return ResponseEntity<FileUploadStatusVO> 上传情况
     */
    @PostMapping(value = "/checkFileMd5")
    @ResponseBody
    public ResponseEntity<FileUploadStatusVO> checkFileMd5(String md5) throws IOException {
        FileUploadStatusVO fileUploadStatusVO = new FileUploadStatusVO();
        Object processingObj = hashOperations.get(Constants.FILE_UPLOAD_STATUS, md5);
        // 文件未上传过
        if (Objects.isNull(processingObj)) {
            fileUploadStatusVO.setStatus(ResultStatusEnum.NO_HAVE);
            return ResponseEntity.ok(fileUploadStatusVO);
        }

        String processingStr = processingObj.toString();
        boolean processing = Boolean.parseBoolean(processingStr);
        String filePath = (String) valueOperations.get(Constants.FILE_MD5_KEY + md5);
        //文件已存在,秒传操作
        if (processing) {
            fileUploadStatusVO.setStatus(ResultStatusEnum.IS_HAVE);
            fileUploadStatusVO.setFilePath(filePath);
            return ResponseEntity.ok(fileUploadStatusVO);
        }

        //文件上传未完整
        List<Integer> missChunkList = new ArrayList<>();
        log.info("filePath == > {}", filePath);
        byte[] completeList = Files.readAllBytes(Paths.get(filePath));

        for (int i = 0; i < completeList.length; i++) {
            if (completeList[i] != Byte.MAX_VALUE) {
                missChunkList.add(i);
            }
        }
        log.info("missChunkList===>{}", missChunkList.size());
        fileUploadStatusVO.setStatus(ResultStatusEnum.ING_HAVE);
        fileUploadStatusVO.setMissChunks(missChunkList);
        return ResponseEntity.ok(fileUploadStatusVO);
    }

    /**
     * 上传文件
     *
     * @param multipartFileParam 上传文件信息
     * @return ResponseEntity<String> resp
     */
    @PostMapping(value = "/fileUpload")
    @ResponseBody
    public ResponseEntity<String> fileUpload(@Validated MultipartFileParam multipartFileParam) {
        log.info("上传分块文件开始,文件名：{}，分块：{}", multipartFileParam.getName(), multipartFileParam.getChunk());
        try {
            storageService.uploadFile(multipartFileParam);
        } catch (IOException e) {
            log.info("上传分块文件开始,文件名：{}，分块：{}", multipartFileParam.getName(), multipartFileParam.getChunk(), e);
            return ResponseEntity.ok().body("上传失败！");
        }
        log.info("上传分块文件成功,文件名：{}，分块：{}", multipartFileParam.getName(), multipartFileParam.getChunk());
        return ResponseEntity.ok().body("上传成功。");
    }

    /**
     * 清空上传文件start
     *
     * @return ResponseEntity<String> resp
     */
    @ApiOperation("清空上传文件")
    @PostMapping(value = "/deleteAll")
    @ResponseBody
    public ResponseEntity<String> deleteAll() {
        log.info("清空上传文件start...");
        storageService.deleteAll();
        log.info("清空上传文件start...");
        return ResponseEntity.ok().body("清空上传文件成功。");
    }

}
