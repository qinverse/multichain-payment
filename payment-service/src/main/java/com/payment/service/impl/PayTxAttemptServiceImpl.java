package com.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.payment.common.base.PayOrderStatusEnum;
import com.payment.common.base.TxAttemptStatusEnum;
import com.payment.mapper.PaySeqMapper;
import com.payment.mapper.PayTxAttemptMapper;
import com.payment.model.dto.TxScanResultDTO;
import com.payment.model.entity.PaySeqEntity;
import com.payment.model.entity.PayTxAttemptEntity;
import com.payment.service.PayTxAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PayTxAttemptServiceImpl implements PayTxAttemptService {

    @Autowired
    private PayTxAttemptMapper payTxAttemptMapper;

    @Autowired
    private PaySeqMapper paySeqMapper;

    @Override
    public void saveOrUpdateTxAttempt(PaySeqEntity seq, TxScanResultDTO scan) {
        LambdaUpdateWrapper<PayTxAttemptEntity> wrapper = Wrappers.lambdaUpdate(PayTxAttemptEntity.class)
                .eq(PayTxAttemptEntity::getPaySeq, seq.getPaySeq())
                .eq(PayTxAttemptEntity::getTxHash, scan.getFinalTxHash());

        PayTxAttemptEntity tx = new PayTxAttemptEntity();
        tx.setTxHash(scan.getFinalTxHash());
        tx.setNonce(scan.getNonce());
        tx.setFee(scan.getFee());
        tx.setBlockNumber(scan.getBlockNumber());
        tx.setStatus(scan.getTxStatus());
        tx.setReplacedBy(scan.getReplacedBy());
        tx.setConfirmedAt(scan.getConfirmedAt());

        int updated = payTxAttemptMapper.update(tx, wrapper);
        if (updated == 0) {
            // 如果没有更新到，说明是新交易，直接插入
            tx.setPaySeq(seq.getPaySeq());
            tx.setFromAccount(seq.getPayAccount());
            tx.setToAccount(seq.getReceiveAccount());
            tx.setAmount(seq.getPayAmount());
            payTxAttemptMapper.insert(tx);
        }

    }

    @Override
    public void createTxAttempt(PaySeqEntity seq) {
        PayTxAttemptEntity tx = new PayTxAttemptEntity();
        tx.setPaySeq(seq.getPaySeq());
        tx.setTxHash(seq.getThirdIdentify()); // 还没有生成 txHash
        tx.setNonce(seq.getNonce());
        tx.setFromAccount(seq.getPayAccount());
        tx.setToAccount(seq.getReceiveAccount());
        tx.setAmount(seq.getPayAmount());
        tx.setStatus(TxAttemptStatusEnum.PENDING.getValue());

        try {
            payTxAttemptMapper.insertOrUpdate(tx);
            log.info("Created initial tx attempt for paySeq={}", seq.getPaySeq());
        } catch (DuplicateKeyException e) {
            log.warn("Initial tx attempt already exists, paySeq={}", seq.getPaySeq());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void scanUpdate(PaySeqEntity seq, TxScanResultDTO scan) {
        // 1. 记录 tx attempt
        this.saveOrUpdateTxAttempt(seq, scan);

        // 2. 只有 success 才回写 pay_seq
        if (scan.isSuccess()) {
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
    }
}
