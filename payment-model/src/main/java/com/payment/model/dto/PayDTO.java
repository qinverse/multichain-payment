package com.payment.model.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 支付方式
 * </p>
 *
 @author qwl
 * @version V1.0
 * @contact
 * @date 2020-06-09

 */
@Data
@EqualsAndHashCode
public class PayDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付方式名称
     */
    private String channelName;

    /**
     * 支付方式code
     */
    private String channelCode;

    /**
     * 支付策略类，用于策略模式实现
     */
    private String strategyBean;
    
    /**
     * 商户id
     */
    private String merchantId;


}
