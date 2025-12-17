package com.payment.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private Long blockNumber;

    /** 链上确认时间 */
    private LocalDateTime confirmedAt;
}
