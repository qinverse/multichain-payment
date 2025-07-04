package com.payment.model.dto;

import lombok.Data;

/**
 * @author qinverse
 * @date 2025/6/18 17:39
 * @description PayQueryDTO 类描述
 */
@Data
public class PayQueryDTO {

    private String paySeq;

    private String chain;

    /**
     * 第三方唯一标识或者连上的交易hash码
     */
    private  String thirdIdentify;

    private Integer currentQuery;
    /**
     * 下次查询时间
     */
    private Integer nextQuery;
}
