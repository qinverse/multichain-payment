package com.payment.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author qinverse
 * @date 2025/6/18 17:36
 * @description 支付结果
 */
@Data
public class PayResultDTO {


    private Integer status;

    private String msg;

    /**
     * 手续费
     */
    private BigDecimal fee;

    private String paySeq;
    private String toAddress;
}
