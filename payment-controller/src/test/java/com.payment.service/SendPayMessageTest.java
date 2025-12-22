package com.payment.service;

import com.payment.common.base.PayOrderStatusEnum;
import com.payment.component.IPayStrategy;
import com.payment.component.spring.SpringContextUtil;
import com.payment.mapper.PaySeqMapper;
import com.payment.model.dto.PayQueryDTO;
import com.payment.model.dto.PayResultDTO;
import com.payment.model.entity.PaySeqEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author qinverse
 * @date 2025/6/24 9:26
 * @description SendPayMessageTest 类描述
 */
@SpringBootTest
public class SendPayMessageTest {


    @Autowired
    PaySeqMapper paySeqMapper;

    @Test
    public void queryPay() {
        String paySeqId = "202507081942538228351582208";
        PaySeqEntity entity = paySeqMapper.selectById(paySeqId);
        IPayStrategy strategy = SpringContextUtil.getBeanByName(entity.getType());
        PayQueryDTO dto = new PayQueryDTO();
        dto.setChain(entity.getType());
        dto.setThirdIdentify(entity.getThirdIdentify());
        PayResultDTO payResultDTO = strategy.queryPay(dto);
        if (PayOrderStatusEnum.PAY_SUCCESS.getValue() == payResultDTO.getStatus()) {
            entity.setStatus(PayOrderStatusEnum.PAY_SUCCESS.getValue());
            entity.setFee(payResultDTO.getFee());
        } else if (PayOrderStatusEnum.PAY_FAIL.getValue() == payResultDTO.getStatus()) {
            entity.setStatus(PayOrderStatusEnum.PAY_FAIL.getValue());
        }
        paySeqMapper.updateById(entity);
    }



    public void sendPayQueryMsg() {
    /*    PaySeqEntity entity = paySeqMapper.selectById("");
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

            //发送查询消息
            Message<PayQueryDTO> messageTo = MessageBuilder.withPayload(payResultDTO).build();
            SendResult result = rocketMQTemplate.syncSend(MqTopic.PAY_QUERY_topic, messageTo, 3000L, message.getCurrentQuery());
            log.info("发送异步查询消息seq-> {}, msgId-> {}", entity.getPaySeq(), result.getMsgId());*/
    }
}
