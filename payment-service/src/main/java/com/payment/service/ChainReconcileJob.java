package com.payment.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.payment.common.base.PayOrderStatusEnum;
import com.payment.component.IPayStrategy;
import com.payment.mapper.PaySeqMapper;
import com.payment.model.dto.TxScanResultDTO;
import com.payment.model.entity.PaySeqEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ChainReconcileJob {

    @Autowired
    private PaySeqMapper paySeqMapper;
    @Autowired
    private IPayStrategy ethStrategy;

    // 每天凌晨3点执行
//    @Scheduled(cron = "0 0 3 * * ?")
// 每 10 分钟执行一次
    @Scheduled(cron = "0 0/10 * * * ?")
    public void reconcile() {

        List<PaySeqEntity> list = paySeqMapper.selectNotFinalConfirmed();

        for (PaySeqEntity seq : list) {

            Optional<TxScanResultDTO> scan =
                    ethStrategy.scanFinalTx(seq);

            scan.ifPresent(r -> {
                if (r.isSuccess()) {
                    paySeqMapper.update(null,
                            Wrappers.lambdaUpdate(PaySeqEntity.class)
                                    .eq(PaySeqEntity::getPaySeq, seq.getPaySeq())
                                    .set(PaySeqEntity::getThirdIdentify, r.getFinalTxHash())
                                    .set(PaySeqEntity::getStatus, PayOrderStatusEnum.PAY_SUCCESS.getValue())
                                    .set(PaySeqEntity::getFee, r.getFee())
                                    .set(PaySeqEntity::getBlockNumber, r.getBlockNumber())
                                    .set(PaySeqEntity::getConfirmedAt, r.getConfirmedAt())
                    );
                }
            });
        }
    }
}
