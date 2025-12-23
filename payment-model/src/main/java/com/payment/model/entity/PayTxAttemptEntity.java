package com.payment.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pay_tx_attempt")
public class PayTxAttemptEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String paySeq;

    private String txHash;
    private String fromAccount;
    private String toAccount;

    private Long nonce;
    private BigDecimal amount;
    private BigDecimal fee;

    private Long blockNumber;

    /**
     * 0 pending
     * 1 success
     * 2 fail
     * 3 replaced
     */
    private Integer status;

    private String replacedBy;

    private String rawTx;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    private LocalDateTime confirmedAt;
}
