package com.payment.component;


import com.payment.model.dto.*;
import com.payment.model.entity.PaySeqEntity;

import java.util.Optional;

public interface IPayStrategy {

    PayRequestResultDTO doPay(PayDTO payDTo);

    PayResultDTO queryPay(PayQueryDTO payQueryDTO);

    <T> PayResultDTO payNotify(T notify);

    default Optional<TxScanResultDTO> scanFinalTx(PaySeqEntity order) {
        return Optional.empty();
    }
}
