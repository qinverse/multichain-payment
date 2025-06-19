package com.payment.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created By MBG-GUI-EXTENSION https://github.com/spawpaw/mybatis-generator-gui-extension
 * Description:
 * 
 *
 * @author 
 */
@Data
public class PaySeqEntity implements Serializable {
    /**
     * 支付流水
     *
     *
     * @mbg.generated Tue Jun 17 17:33:45 GMT+08:00 2025
     */
    private String paySeq;

    /**
     * 第三方支付标识
     *
     *
     * @mbg.generated Tue Jun 17 17:33:45 GMT+08:00 2025
     */
    private String thirdIdentify;

    /**
     * 支付账户
     *
     *
     * @mbg.generated Tue Jun 17 17:33:45 GMT+08:00 2025
     */
    private String payAccount;

    /**
     * 收款账户
     *
     *
     * @mbg.generated Tue Jun 17 17:33:45 GMT+08:00 2025
     */
    private String receiveAccount;

    /**
     * 支付数额
     *
     *
     * @mbg.generated Tue Jun 17 17:33:45 GMT+08:00 2025
     */
    private BigDecimal payAmount;

    /**
     * 其他费用例如gas
     *
     *
     * @mbg.generated Tue Jun 17 17:33:45 GMT+08:00 2025
     */
    private BigDecimal fee;

    /**
     * 支付状态 0发起 1 支付中 2 支付成功 3 支付失败
     *
     *
     * @mbg.generated Tue Jun 17 17:33:45 GMT+08:00 2025
     */
    private Integer status;

    /**
     * 支付类别，1rmb支付 2 eth
     *
     *
     * @mbg.generated Tue Jun 17 17:33:45 GMT+08:00 2025
     */
    private Integer type;

    /**
     * 支付参数
     *
     *
     * @mbg.generated Tue Jun 17 17:33:45 GMT+08:00 2025
     */
    private String payParam;

    /**
     * 发起支付时间
     *
     *
     * @mbg.generated Tue Jun 17 17:33:45 GMT+08:00 2025
     */
    private Date payTime;

    /**
     * 第三方查询结果报文
     *
     *
     * @mbg.generated Tue Jun 17 17:33:45 GMT+08:00 2025
     */
    private String thirdParam;

    /**
     * 查询次数
     *
     *
     * @mbg.generated Tue Jun 17 17:33:45 GMT+08:00 2025
     */
    private Integer queryCount;

    /**
     * 账单日
     *
     *
     * @mbg.generated Tue Jun 17 17:33:45 GMT+08:00 2025
     */
    private String billDate;

    /**
     * 修改时间
     *
     *
     * @mbg.generated Tue Jun 17 17:33:45 GMT+08:00 2025
     */
    private Date modifyTime;

    /**
     * 支付方式
     */
    private String payWay;

    /**
     *
     * @mbg.generated Tue Jun 17 17:33:45 GMT+08:00 2025
     */
    private static final long serialVersionUID = 1L;

}