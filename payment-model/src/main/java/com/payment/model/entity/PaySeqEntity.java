package com.payment.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created By MBG-GUI-EXTENSION https://github.com/spawpaw/mybatis-generator-gui-extension
 * Description:
 * 
 *
 * @author 
 */
@Data
@TableName(value = "pay_seq")
public class PaySeqEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 支付流水号（业务主键） */
    private String paySeq;

    /** 付款地址 */
    private String payAccount;

    /** 收款地址 */
    private String receiveAccount;

    /** 支付金额 */
    private BigDecimal payAmount;

    /** 链类型，如 eth */
    private String type;

    /**
     * 支付状态
     * 0-发起
     * 1-支付中
     * 2-成功
     * 3-失败
     * 4-超时
     */
    private Integer status;

    /** 最终确认的链上交易 hash */
    private String thirdIdentify;

    /** 最终交易 nonce */
    private Long nonce;

    /** 实际链上手续费 */
    private BigDecimal fee;

    /** 最终确认区块高度 */
    private Long blockNumber;

    /** 链上确认时间 */
    private LocalDateTime confirmedAt;

    /** 支付扩展参数（下单快照） */
    private String payParam;

    /** 第三方 / 链上结果摘要 */
    private String thirdParam;

    /** 账单日 */
    private String billDate;

    /** 支付发起时间 */
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /** 最后更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}
