package com.coder.lee.filesystem.service;


import com.coder.lee.filesystem.request.MultipartFileParam;

import java.io.IOException;

/**
 * Description: StorageServiceImpl
 * Copyright: Copyright (c)
 * Company: Ruijie Co., Ltd.
 * Date:    2020/11/1 14:36
 *
 * @author coderLee23
 */
public interface StorageService {

    /**
     * 删除全部数据
     */
    void deleteAll();

    /**
     * 上传文件方法2
     * 处理文件分块，基于MappedByteBuffer来实现文件的保存
     *
     * @param param
     * @throws IOException
     */
    void uploadFile(MultipartFileParam param) throws IOException;

}