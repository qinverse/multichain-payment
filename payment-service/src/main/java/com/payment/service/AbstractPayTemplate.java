package com.payment.service;

import com.payment.common.result.ResultDTO;
import com.payment.component.IPayStrategy;
import com.payment.component.spring.SpringContextUtil;
import com.payment.common.base.DelayLevelEnum;
import com.payment.common.base.PayOrderStatusEnum;
import com.payment.common.util.IDUtils;
import com.payment.mapper.PaySeqMapper;
import com.payment.model.dto.*;
import com.payment.model.dto.mq.MqTopic;
import com.payment.model.entity.PaySeqEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public abstract class AbstractPayTemplate {

    @Autowired
    protected PaySeqMapper paySeqMapper;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public PayResultDTO toPay(PayDTO dto) {

        // 1. 参数校验
        checkParam(dto);

        // 2. 创建支付单
        String paySeq = IDUtils.snowFlakeIdGenerator();
        PaySeqEntity entity = new PaySeqEntity();
        entity.setPaySeq(paySeq);
        entity.setPayAccount(dto.getFrom());
        entity.setPayAmount(dto.getAmount());
        entity.setReceiveAccount(dto.getToAddress());
        entity.setCreatedTime(Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime());
        entity.setModifyTime(Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime());
        entity.setStatus(PayOrderStatusEnum.PAY_PENDING.getValue());
        entity.setType(dto.getChannelCode());

        paySeqMapper.insert(entity);

        // 3. 发起链上支付（不判断结果）
        IPayStrategy strategy = SpringContextUtil.getBeanByName(dto.getChannelCode());

        PayRequestResultDTO requestResult = strategy.doPay(dto);
        if (requestResult == null) {
            //由前端唤起支付，不需要后端交互，直接返回就可以了
            PayResultDTO result = new PayResultDTO();
            result.setPaySeq(paySeq);
            result.setStatus(entity.getStatus());
            result.setToAddress(entity.getReceiveAccount());
            return result;
        }

        // 4. 进入“可监听 / 可查询态”
        entity.setStatus(PayOrderStatusEnum.PAY_TX_SUBMITTED.getValue());
        entity.setThirdIdentify(requestResult.getThirdIdentify());
        entity.setReceiveAccount(dto.getToAddress());
        entity.setModifyTime(Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime());

        paySeqMapper.updateById(entity);

        // 5. 发送延迟查询消息
        sendOrderedDelayMessage(entity);

        // 6. 返回给前端
        PayResultDTO result = new PayResultDTO();
        result.setPaySeq(paySeq);
        result.setStatus(entity.getStatus());
        result.setToAddress(entity.getReceiveAccount());
        return result;
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
        SendResult result = rocketMQTemplate.syncSend(MqTopic.PAY_QUERY_TOPIC, message, 3000L, DelayLevelEnum.LEVEL_6.getLevel());
        log.info("发送异步查询消息seq-> {}, msgId-> {}", entity.getPaySeq(), result.getMsgId());
    }


    abstract void checkParam(PayDTO dto);

    public void updateTxtHash(PaySeqEntity paySeq) {
        PaySeqEntity lo = paySeqMapper.selectById(paySeq.getPaySeq());
        lo.setThirdIdentify(paySeq.getThirdIdentify());
        paySeqMapper.updateById(lo);
    }

    public void refresOrder(String paySeq) {
        PaySeqEntity lo = paySeqMapper.selectById(paySeq);
        IPayStrategy strategy = SpringContextUtil.getBeanByName(lo.getType());
        Optional<TxScanResultDTO> txtHash = strategy.scanFinalTx(lo);
        if (txtHash.isPresent()) {
            lo.setThirdIdentify(txtHash.get().getFinalTxHash());
            lo.setCreatedTime(txtHash.get().getConfirmedAt());
            lo.setBlockNumber(txtHash.get().getBlockNumber());
            paySeqMapper.updateById(lo);
            this.sendOrderedDelayMessage(lo);
        }
    }
}
