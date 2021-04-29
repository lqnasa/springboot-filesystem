package com.coder.lee.filesystem.vo;

import com.coder.lee.filesystem.enums.ResultStatusEnum;
import lombok.Data;

import java.util.List;

/**
 * Description: FileUploadStatusVO
 * Copyright: Copyright (c)
 * Company: Ruijie Co., Ltd.
 * Date:    2020/11/1 16:01
 *
 * @author coderLee23
 */
@Data
public class FileUploadStatusVO {

    private ResultStatusEnum status;

    private String filePath;

    private List<Integer> missChunks;

}
