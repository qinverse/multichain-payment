package com.payment.service;

import com.payment.component.IPayStrategy;
import com.payment.component.spring.SpringContextUtil;
import com.payment.common.base.DelayLevelEnum;
import com.payment.common.base.PayOrderStatusEnum;
import com.payment.common.util.IDUtils;
import com.payment.mapper.PaySeqMapper;
import com.payment.model.dto.PayDTO;
import com.payment.model.dto.PayQueryDTO;
import com.payment.model.dto.PayRequestResultDTO;
import com.payment.model.dto.PayResultDTO;
import com.payment.model.dto.mq.MqTopic;
import com.payment.model.entity.PaySeqEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public abstract class AbstractPayTemplate {

    @Autowired
    protected PaySeqMapper paySeqMapper;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public PayResultDTO toPay(PayDTO dto) {
        String id = IDUtils.snowFlakeIdGenerator();
        PaySeqEntity entity = new PaySeqEntity();
        entity.setPaySeq(id);
        entity.setPayAccount(dto.getFrom());
        entity.setPayAmount(dto.getAmount());
        entity.setPayTime(new Date());
        entity.setReceiveAccount(dto.getToAddress());
        entity.setModifyTime(new Date());
        entity.setStatus(PayOrderStatusEnum.PAY_PENDING.getValue());
        entity.setQueryCount(0);
        entity.setType(dto.getChannelCode());
        paySeqMapper.insert(entity);
        IPayStrategy strategy = SpringContextUtil.getBeanByName(dto.getChannelCode());
        PayRequestResultDTO payRequestResultDTO = strategy.doPay(dto);
        //更新支付状态为listening
        entity.setStatus(payRequestResultDTO.getStatus());
        entity.setThirdIdentify(payRequestResultDTO.getThirdIdentify());
        paySeqMapper.updateById(entity);
        PayResultDTO payResultDTO = new PayResultDTO();
        payResultDTO.setStatus(payResultDTO.getStatus());
        sendOrderedDelayMessage(entity);
        return payResultDTO;
    }

    private void sendOrderedDelayMessage(PaySeqEntity entity) {
        PayQueryDTO payQueryDTO = new PayQueryDTO();
        payQueryDTO.setPaySeq(entity.getPaySeq());
        payQueryDTO.setChain(entity.getType());
        payQueryDTO.setThirdIdentify(entity.getThirdIdentify());
        payQueryDTO.setCurrentQuery(DelayLevelEnum.LEVEL_6.getLevel());
        payQueryDTO.setNextQuery(DelayLevelEnum.LEVEL_9.getLevel());
        //发送查询消息
        Message<PayQueryDTO> message = MessageBuilder.withPayload(payQueryDTO).build();
        SendResult result = rocketMQTemplate.syncSend(MqTopic.PAY_QUERY_topic, message, 3000L, DelayLevelEnum.LEVEL_6.getLevel());
        log.info("发送异步查询消息seq-> {}, msgId-> {}", entity.getPaySeq(), result.getMsgId());
    }


    abstract void checkParam(PayDTO dto);
}
