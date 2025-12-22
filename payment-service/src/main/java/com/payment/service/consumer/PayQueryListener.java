package com.payment.service.consumer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.payment.common.base.DelayLevelEnum;
import com.payment.common.base.PayOrderStatusEnum;
import com.payment.component.IPayStrategy;
import com.payment.component.spring.SpringContextUtil;
import com.payment.mapper.PaySeqMapper;
import com.payment.mapper.PayTxAttemptMapper;
import com.payment.model.dto.PayQueryDTO;
import com.payment.model.dto.PayResultDTO;
import com.payment.model.dto.TxScanResultDTO;
import com.payment.model.dto.mq.MqGroup;
import com.payment.model.dto.mq.MqTopic;
import com.payment.model.entity.PaySeqEntity;
import com.payment.model.entity.PayTxAttemptEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author qinverse
 * @date 2025/6/18 17:12
 * @description PayQueryListener 类描述
 */
@Slf4j
@Service
@RocketMQMessageListener(
        consumerGroup = MqGroup.GROUP_MULTICHAIN_PAY,
        topic = MqTopic.PAY_QUERY_TOPIC
)
public class PayQueryListener implements RocketMQListener<PayQueryDTO> {

    private static final long SCAN_INTERVAL_MS = 5 * 60 * 1000L; // 5分钟
    private static final int MAX_SCAN_COUNT = 6; // 30分钟

    @Autowired
    private PaySeqMapper paySeqMapper;
    @Autowired
    private PayTxAttemptMapper payTxAttemptMapper;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(PayQueryDTO msg) {

        PaySeqEntity seq = paySeqMapper.selectById(msg.getPaySeq());
        if (seq == null || PayOrderStatusEnum.isFinal(seq.getStatus())) {
            return;
        }

        IPayStrategy strategy = SpringContextUtil.getBeanByName(seq.getType());

        // ===== ① 没 final tx → 扫描补偿 =====
        if (seq.getThirdIdentify() == null) {
            Optional<TxScanResultDTO> scanOpt = strategy.scanFinalTx(seq);

            scanOpt.ifPresent(scan -> {
                // 1. 记录 tx attempt
                saveTxAttempt(seq, scan);

                // 2. 只有 success 才回写 pay_seq
                if (scan.isSuccess()) {
                    updatePaySeqFinal(seq, scan);
                }
            });
        }

        // ===== ② 有 final tx → 精确查询 =====
        if (seq.getThirdIdentify() != null) {
            PayQueryDTO q = new PayQueryDTO();
            q.setChain(seq.getType());
            q.setThirdIdentify(seq.getThirdIdentify());

            PayResultDTO r = strategy.queryPay(q);

            if (PayOrderStatusEnum.isFinal(r.getStatus())) {
                updatePaySeqStatus(seq, r);
                return;
            }
        }

        // ===== ③ 继续延迟 =====
        msg.setScanCount(
                Optional.ofNullable(msg.getScanCount()).orElse(0) + 1
        );

        if (msg.getScanCount() >= MAX_SCAN_COUNT) {
            timeout(seq);
            return;
        }

        msg.setNextScanTime(System.currentTimeMillis() + SCAN_INTERVAL_MS);
        resend(msg);
    }

    /* =================== private =================== */

    private void saveTxAttempt(PaySeqEntity seq, TxScanResultDTO scan) {

        PayTxAttemptEntity tx = new PayTxAttemptEntity();
        tx.setPaySeq(seq.getPaySeq());
        tx.setTxHash(scan.getFinalTxHash());
        tx.setFromAccount(seq.getPayAccount());
        tx.setToAccount(seq.getReceiveAccount());
        tx.setNonce(scan.getNonce());
        tx.setAmount(seq.getPayAmount());
        tx.setFee(scan.getFee());
        tx.setBlockNumber(scan.getBlockNumber());
        tx.setStatus(scan.getTxStatus());
        tx.setReplacedBy(scan.getReplacedBy());
        tx.setCreatedTime(LocalDateTime.now());
        tx.setConfirmedAt(scan.getConfirmedAt());

        try {
            payTxAttemptMapper.insert(tx);
        } catch (DuplicateKeyException e) {
            // tx_hash 已存在，直接忽略
            log.debug("tx attempt already exists, txHash={}", tx.getTxHash());
        }
    }

    private void updatePaySeqFinal(PaySeqEntity seq, TxScanResultDTO scan) {

        paySeqMapper.update(null,
                Wrappers.lambdaUpdate(PaySeqEntity.class)
                        .eq(PaySeqEntity::getPaySeq, seq.getPaySeq())
                        .isNull(PaySeqEntity::getThirdIdentify)
                        .set(PaySeqEntity::getThirdIdentify, scan.getFinalTxHash())
                        .set(PaySeqEntity::getStatus, PayOrderStatusEnum.PAY_SUCCESS.getValue())
                        .set(PaySeqEntity::getBlockNumber, scan.getBlockNumber())
                        .set(PaySeqEntity::getConfirmedAt, scan.getConfirmedAt())
                        .set(PaySeqEntity::getFee, scan.getFee())
        );
    }

    private void updatePaySeqStatus(PaySeqEntity seq, PayResultDTO r) {

        paySeqMapper.update(null,
                Wrappers.lambdaUpdate(PaySeqEntity.class)
                        .eq(PaySeqEntity::getPaySeq, seq.getPaySeq())
                        .set(PaySeqEntity::getStatus, r.getStatus())
                        .set(r.getFee() != null, PaySeqEntity::getFee, r.getFee())
                        .set(PaySeqEntity::getBlockNumber, r.getBlockNumber())
                        .set(PaySeqEntity::getConfirmedAt, r.getConfirmedAt())
        );
    }

    private void timeout(PaySeqEntity seq) {
        paySeqMapper.update(null,
                Wrappers.lambdaUpdate(PaySeqEntity.class)
                        .eq(PaySeqEntity::getPaySeq, seq.getPaySeq())
                        .set(PaySeqEntity::getStatus, PayOrderStatusEnum.PAY_TIMEOUT.getValue())
        );
    }

    private void resend(PayQueryDTO msg) {
        rocketMQTemplate.syncSend(
                MqTopic.PAY_QUERY_TOPIC,
                MessageBuilder.withPayload(msg).build()
        );
    }
}
