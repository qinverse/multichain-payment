package com.payment.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TxScanResultDTO {

    private String originTxHash;
    private String finalTxHash;

    private boolean replaced;
    private boolean success;

    private Long nonce;
    private BigDecimal fee;
    private Long blockNumber;
    private LocalDateTime confirmedAt;

    private Integer txStatus; // pending/success/fail/replaced
    private String replacedBy;
}
