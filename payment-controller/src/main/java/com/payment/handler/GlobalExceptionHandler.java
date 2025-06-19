package com.payment.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import com.payment.common.exception.BizException;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
/**
 * @author qinverse
 * @date 2025/6/19 18:08
 * @description 全局异常捕获
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 自定义业务异常处理
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBizException(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return buildResponse(400, e.getMessage());
    }

    // 参数校验异常（@Valid + 对象接收）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return buildResponse(400, message);
    }

    // 参数校验异常（@Valid + 单个参数）
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleConstraintViolationException(ConstraintViolationException e) {
        return buildResponse(400, e.getMessage());
    }

    // 处理绑定异常
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return buildResponse(400, message);
    }

    // 兜底系统异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGenericException(Exception e) {
        log.error("系统异常", e);
        return buildResponse(500, "系统异常，请联系管理员");
    }

    // 构造返回结构
    private Map<String, Object> buildResponse(int code, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("msg", message);
        return map;
    }
}
