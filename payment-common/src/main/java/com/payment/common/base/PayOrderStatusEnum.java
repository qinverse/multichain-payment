package com.payment.common.base;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 支付状态枚举
 * @author qwl
 * @version V1.0
 * PAY_PENDING
 *    ↓ 用户确认钱包
 * PAY_TX_SUBMITTED
 *    ↓ 上链成功
 * PAY_CONFIRMING
 *    ↓ N 个区块确认
 * PAY_SUCCESS
 *    ↓ 商户确认
 * PAY_FINISHED
 * 异常分支
 * PAY_TX_SUBMITTED
 *    ↓ tx revert / 校验失败
 * PAY_FAIL
 * 超时分支：
 * PAY_PENDING
 *    ↓ 超过 15 分钟
 * PAY_TIMEOUT
 *
 * 退款分支：
 * PAY_SUCCESS
 *    ↓ 发起退款
 * PAY_REFUND_PENDING
 *    ↓ 链上退款成功
 * PAY_REFUND_SUCCESS
 * @date: 2020年06月09日 11时51分

 */
public enum PayOrderStatusEnum {

    // ========= 支付主流程 =========

    PAY_PENDING(0, "等待支付中"),               // 订单已创建，未发起链上交易

    PAY_TX_SUBMITTED(1, "交易已提交，等待链上确认"), // 已收到 txHash（原 PAY_LISTENLING）

    PAY_CONFIRMING(2, "链上确认中"),             // 已上链，未达到确认数

    PAY_SUCCESS(3, "支付成功"),                  // 链上确认完成

    PAY_FAIL(-1, "支付失败"),                    // 明确失败（revert / 校验不通过）

    PAY_TIMEOUT(4, "支付超时关闭"),              // 超过支付窗口未付款

    // ========= 终态 =========

    PAY_FINISHED(5, "交易结束，不可退款"),        // 商户确认完成

    // ========= 退款流程 =========

    PAY_REFUND_PENDING(10, "退款中"),

    PAY_REFUND_SUCCESS(11, "退款成功"),

    PAY_REFUND_FAIL(12, "退款失败"),

    PAY_REFUND_CLOSED(13, "退款关闭"),

    // ========= 异常 / 人工处理 =========

    PAY_ABNORMAL(20, "支付异常，需人工处理"),

    PAY_RISK_REJECT(21, "风控拒绝");

    private final int value;
    private final String name;

    PayOrderStatusEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static PayOrderStatusEnum getEnumByValue(int value) {
        for (PayOrderStatusEnum ele : values()) {
            if (ele.value == value) {
                return ele;
            }
        }
        return null;
    }

    /* ================== 终态集合 ================== */

    private static final Set<Integer> FINAL_STATUS = new HashSet<>(
            Arrays.asList(
                    PAY_SUCCESS.value,
                    PAY_FAIL.value,
                    PAY_TIMEOUT.value,
                    PAY_FINISHED.value
            )
    );

    /**
     * 是否终态
     */
    public static boolean isFinal(Integer status) {
        if (status == null) {
            return false;
        }
        return FINAL_STATUS.contains(status);
    }

    /**
     * 是否进行中状态
     */
    public static boolean isProcessing(Integer status) {
        if (status == null) {
            return false;
        }
        return !isFinal(status);
    }
}

