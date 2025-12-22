package com.payment.model.dto.mq;

/**
 * @author qinverse
 * @date 2025/6/18 16:53
 * @description 消息主题定义
 */
public class MqTopic {

    /**
     * 支付成功消息
     */
    public static final String PAY_SUCCESS_TOPIC = "multichan_payment_pay_success_topic";

    /**
     * 支付查询
     */
    public static final String PAY_QUERY_TOPIC = "multichan_payment_pay_query_topic";
}
