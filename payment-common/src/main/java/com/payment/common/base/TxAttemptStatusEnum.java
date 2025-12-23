package com.payment.common.base;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 链上交易尝试状态枚举
 * 对账逻辑：
 *  - PENDING: 发起交易但 txHash 还没生成，处理中
 *  - SUCCESS: 链上交易已确认，最终状态
 *  - FAILED: 链上交易失败或 revert，最终状态
 *  - REPLACED: 被替换交易，最终状态
 */
public enum TxAttemptStatusEnum {

    // ========= 支付链上流程 =========
    PENDING(0, "等待链上交易生成"),      // 发起交易但 txHash 还没生成
    SUBMITTED(1, "交易已提交，等待链上确认"), // tx 已生成，正在等待上链
    CONFIRMING(2, "链上确认中"),        // 已上链，未达到确认数
    SUCCESS(3, "链上确认成功"),          // 链上交易确认完成

    // ========= 异常终态 =========
    FAILED(-1, "链上交易失败"),          // 链上失败或 revert
    REPLACED(4, "交易被替换");           // 被替换交易（如 EIP-1559 中 nonce 替换）

    private final int value;
    private final String name;

    TxAttemptStatusEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static TxAttemptStatusEnum getEnumByValue(int value) {
        for (TxAttemptStatusEnum status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }

    /* ================== 终态集合 ================== */
    private static final Set<Integer> FINAL_STATUS = new HashSet<>(Arrays.asList(
            SUCCESS.getValue(),
            FAILED.getValue(),
            REPLACED.getValue()
    ));

    /**
     * 是否终态
     */
    public static boolean isFinal(Integer status) {
        if (status == null) return false;
        return FINAL_STATUS.contains(status);
    }

    /**
     * 是否处理中状态
     */
    public static boolean isProcessing(Integer status) {
        if (status == null) return false;
        return !isFinal(status);
    }
}
