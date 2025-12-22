package com.payment.common.result;

import lombok.Data;

@Data
public class ResultDTO<T> {

    /**
     * 是否业务成功
     */
    private boolean success;

    /**
     * 业务码（非 HTTP）
     */
    private String code;

    /**
     * 提示信息（可直接给前端展示）
     */
    private String msg;

    /**
     * 业务数据
     */
    private T data;

    /* ================= 常量 ================= */

    public static final String SUCCESS = "2000";
    public static final String FAIL = "5000";

    /* ================= 构造器 ================= */

    private ResultDTO(boolean success, String code, String msg, T data) {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /* ================= 成功 ================= */

    public static <T> ResultDTO<T> success(T data) {
        return new ResultDTO<>(true, SUCCESS, "ok", data);
    }

    public static <T> ResultDTO<T> success(String msg, T data) {
        return new ResultDTO<>(true, SUCCESS, msg, data);
    }

    public static ResultDTO<Void> success() {
        return new ResultDTO<>(true, SUCCESS, "ok", null);
    }

    /* ================= 失败 ================= */

    public static ResultDTO<Void> fail(String code, String msg) {
        return new ResultDTO<>(false, code, msg, null);
    }

    public static <T> ResultDTO<T> fail(String code, String msg, T data) {
        return new ResultDTO<>(false, code, msg, data);
    }
}
