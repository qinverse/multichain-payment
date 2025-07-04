package com.payment.service.consumer;

import com.payment.component.IPayStrategy;
import com.payment.component.spring.SpringContextUtil;
import com.payment.common.base.DelayLevelEnum;
import com.payment.common.base.PayOrderStatusEnum;
import com.payment.mapper.PaySeqMapper;
import com.payment.model.dto.PayQueryDTO;
import com.payment.model.dto.PayResultDTO;
import com.payment.model.dto.mq.MqGroup;
import com.payment.model.dto.mq.MqTopic;
import com.payment.model.entity.PaySeqEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
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

    @Autowired
    private PaySeqMapper paySeqMapper;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(PayQueryDTO message) {
        log.info("收到查询信息-> {}", message);
        PaySeqEntity entity = paySeqMapper.selectById(message.getPaySeq());
        if (entity == null) {
            log.error("本地订单异常，查询不到-{}", message);
            return;
        }
        IPayStrategy strategy = SpringContextUtil.getBeanByName(entity.getType());
        PayQueryDTO dto = new PayQueryDTO();
        dto.setChain(entity.getType());
        dto.setThirdIdentify(entity.getThirdIdentify());
        if (PayOrderStatusEnum.PAY_SUCCESS.getValue() == entity.getStatus()
                || PayOrderStatusEnum.PAY_FAIL.getValue() == entity.getStatus()
                || PayOrderStatusEnum.PAY_TRADE_CLOSE.getValue() == entity.getStatus()
        ) {
            return;
        }
        PayResultDTO payResultDTO = strategy.queryPay(dto);
        if (PayOrderStatusEnum.PAY_SUCCESS.getValue() == payResultDTO.getStatus()) {
            entity.setStatus(PayOrderStatusEnum.PAY_SUCCESS.getValue());
        } else if (PayOrderStatusEnum.PAY_FAIL.getValue() == payResultDTO.getStatus()) {
            entity.setStatus(PayOrderStatusEnum.PAY_FAIL.getValue());
        } else if (message.getNextQuery() == null) {
            entity.setStatus(PayOrderStatusEnum.PAY_TRADE_CLOSE.getValue());
        } else {
            int nextQuery = message.getCurrentQuery() + message.getNextQuery() - 1;
            if (message.getCurrentQuery().equals(message.getNextQuery())
                    && message.getNextQuery().equals(DelayLevelEnum.LEVEL_18)) {
                return;
            }
            if (nextQuery > DelayLevelEnum.LEVEL_18.getLevel()) {
                nextQuery = DelayLevelEnum.LEVEL_18.getLevel();
            }
            message.setCurrentQuery(message.getNextQuery());
            message.setNextQuery(nextQuery);
            //发送查询消息
            Message<PayQueryDTO> messageTo = MessageBuilder.withPayload(message).build();
            SendResult result = rocketMQTemplate.syncSend(MqTopic.PAY_QUERY_topic, messageTo, 3000L, message.getCurrentQuery());
            log.info("发送异步查询消息seq-> {}, msgId-> {}", entity.getPaySeq(), result.getMsgId());
        }

    }


}
