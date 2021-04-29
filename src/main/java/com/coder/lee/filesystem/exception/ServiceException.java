package com.coder.lee.filesystem.exception;

import javax.servlet.http.HttpServletResponse;

/**
 * Description: ServiceException
 * Copyright: Copyright (c) 2020
 * Company: Ruijie Co., Ltd.
 * Create Time: 2020/6/22
 *
 * @author coderLee23
 */
public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 2359767895161832954L;

    private final int resultCode;

    public int getResultCode() {
        return resultCode;
    }

    public ServiceException(String message) {
        super(message);
        this.resultCode = HttpServletResponse.SC_EXPECTATION_FAILED;
    }

    public ServiceException(int resultCode, String msg) {
        super(msg);
        this.resultCode = resultCode;
    }

    public ServiceException(int resultCode, Throwable cause) {
        super(cause);
        this.resultCode = resultCode;
    }

    public ServiceException(String msg, Throwable cause) {
        super(msg, cause);
        this.resultCode = HttpServletResponse.SC_EXPECTATION_FAILED;
    }

    /**
     * for better performance
     *
     * @return Throwable
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    public Throwable doFillInStackTrace() {
        return super.fillInStackTrace();
    }
}
