package com.coder.lee.filesystem.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Description: Function Description
 * Copyright: Copyright (c)
 * Company: Ruijie Co., Ltd.
 * Date:    2020/11/1 14:36
 *
 * @Author: coderLee23
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ResultStatusEnum {
    /**
     * 文件已存在
     */
    IS_HAVE(100, "文件已存在"),
    /**
     * 文件未上传
     */
    NO_HAVE(101, "文件未上传"),
    /**
     * 文件上传未完整
     */
    ING_HAVE(102, "文件上传未完整");


    private final int value;

    private final String reasonPhrase;


    ResultStatusEnum(int value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    public int getValue() {
        return value;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }
}