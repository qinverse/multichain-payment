package com.payment.common.exception;

/**
 * 业务异常（可预期、可提示给用户）
 */
public class BizException extends RuntimeException {

    /**
     * 业务错误码（非 HTTP）
     */
    private final String code;

    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 默认业务异常
     */
    public BizException(String message) {
        this("BIZ_ERROR", message);
    }

    public String getCode() {
        return code;
    }
}
