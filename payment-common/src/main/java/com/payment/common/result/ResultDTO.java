package com.payment.common.result;

import lombok.Data;

/**
 * @author qinverse
 * @date 2025/6/23 17:04
 * @description ResultDTO 类描述
 */
@Data
public class ResultDTO<T> {
    private String code;

    private String msg;

    private T data;

    public static final String SUCCESS = "2000";


    public static ResultDTO buildResponse(String code, String message) {
        ResultDTO resultDTO = new ResultDTO<>();
        resultDTO.setCode(code);
        resultDTO.setMsg(message);
        return resultDTO;
    }

    public static <D> ResultDTO buildSuccess(Class<D> data) {
        ResultDTO resultDTO = new ResultDTO<>();
        resultDTO.setCode(SUCCESS);
        resultDTO.setMsg("ok");
        resultDTO.setData(data);
        return resultDTO;
    }
}
