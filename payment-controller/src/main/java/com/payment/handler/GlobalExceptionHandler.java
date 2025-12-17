package com.payment.handler;

import com.payment.common.exception.BizException;
import com.payment.common.result.ResultDTO;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ================= 业务异常 =================
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDTO<Void> handleBizException(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResultDTO.fail(
                e.getCode() != null ? e.getCode() : "BIZ_ERROR",
                e.getMessage()
        );
    }

    /**
     * ================= 参数校验异常（对象） =================
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDTO<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldError()
                .getDefaultMessage();
        return ResultDTO.fail("PARAM_ERROR", message);
    }

    /**
     * ================= 参数校验异常（单参数） =================
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDTO<Void> handleConstraintViolationException(ConstraintViolationException e) {
        return ResultDTO.fail("PARAM_ERROR", e.getMessage());
    }

    /**
     * ================= 绑定异常 =================
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDTO<Void> handleBindException(BindException e) {
        String message = e.getBindingResult()
                .getFieldError()
                .getDefaultMessage();
        return ResultDTO.fail("PARAM_ERROR", message);
    }

    /**
     * ================= 系统兜底异常 =================
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultDTO<Void> handleGenericException(Exception e) {
        log.error("系统异常", e);
        return ResultDTO.fail(
                "SYSTEM_ERROR",
                "系统异常，请稍后重试"
        );
    }
}
