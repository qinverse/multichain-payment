package com.payment.common.exception;

/**
 * @author qinverse
 * @date 2025/6/19 18:11
 * @description BizException 类描述
 */
public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }
}