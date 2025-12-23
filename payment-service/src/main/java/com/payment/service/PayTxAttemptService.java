package com.payment.service;

import com.payment.model.dto.TxScanResultDTO;
import com.payment.model.entity.PaySeqEntity;

public interface PayTxAttemptService {

    /**
     * 链上查询记录
     * @param seq
     * @param scan
     */
     void saveOrUpdateTxAttempt(PaySeqEntity seq, TxScanResultDTO scan);

    /**
     * 创建记录
     * @param seq
     */
    void createTxAttempt(PaySeqEntity seq);

    void scanUpdate(PaySeqEntity seq, TxScanResultDTO scan);
}
