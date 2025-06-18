package com.payment.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author qinverse
 * @date 2025/6/18 17:38
 * @description 支付查询结果
 */
@Data
public class PayRequestResultDTO {
    /**
     * 第三方唯一标识或者连上的交易hash码
     */
    private  String thirdIdentify;

    private Integer status;

    private String msg;

    /**
     * 手续费
     */
    private BigDecimal fee;
}
