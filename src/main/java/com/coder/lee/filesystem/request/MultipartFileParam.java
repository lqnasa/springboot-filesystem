package com.coder.lee.filesystem.request;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Description: MultipartFileParam
 * Copyright: Copyright (c) 2020
 * Company: Ruijie Co., Ltd.
 * Create Time: 2020/11/1
 *
 * @author coderLee23
 */
@Data
public class MultipartFileParam {

    /**
     * 用户id
     */
    @NotBlank
    private String uid;
    /**
     * 任务ID
     */
    @NotBlank
    private String id;
    /**
     * 总分片数量
     */
    @Min(value = 0)
    private int chunks;
    /**
     * 当前为第几块分片
     */
    @Min(value = 0)
    private int chunk;
    /**
     * 当前分片大小
     */
    @Min(value = 0)
    private long size;
    /**
     * 文件名
     */
    @NotBlank
    private String name;
    /**
     * 分片对象
     */
    @NotNull
    private MultipartFile file;
    /**
     * MD5
     */
    @NotBlank
    private String md5;
}