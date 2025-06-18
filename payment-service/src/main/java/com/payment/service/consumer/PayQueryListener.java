package com.payment.service.consumer;

import com.payment.model.dto.PayQueryDTO;
import com.payment.model.dto.mq.MqGroup;
import com.payment.model.dto.mq.MqTopic;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * @author qinverse
 * @date 2025/6/18 17:12
 * @description PayQueryListener 类描述
 */
@Slf4j
@Service
@RocketMQMessageListener(consumerGroup = MqGroup.GROUP_MULTICHAIN_PAY, topic = MqTopic.PAY_SUCCESS_TOPIC)
public class PayQueryListener implements RocketMQListener<PayQueryDTO> {
    @Override
    public void onMessage(PayQueryDTO message) {

    }
}
