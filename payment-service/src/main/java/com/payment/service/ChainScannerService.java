package com.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.payment.common.base.PayOrderStatusEnum;
import com.payment.mapper.PaySeqMapper;
import com.payment.model.entity.PaySeqEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Service
public class ChainScannerService {

    private final Web3j web3j =  Web3j.build(new HttpService("https://sepolia.infura.io/v3/23599b1b46fa40c99a81c4a376c8fdba"));

    @Autowired
    private PaySeqMapper paySeqMapper;

    // 商户收款地址（区分大小写不敏感）
    private final String merchantAddress = "0xYourMerchantAddress".toLowerCase();

    // 订单匹配最大区块回溯数（可调）
    private final int scanBlockCount = 50;


    @Scheduled(fixedDelay = 60 * 1000) // 每1分钟执行一次
    public void scanRecentBlocksAndMatchOrders() {
        try {
            BigInteger latestBlock = web3j.ethBlockNumber().send().getBlockNumber();
            BigInteger startBlock = latestBlock.subtract(BigInteger.valueOf(scanBlockCount));
            if (startBlock.compareTo(BigInteger.ZERO) < 0) startBlock = BigInteger.ZERO;

            System.out.println("开始扫描区块: " + startBlock + " - " + latestBlock);

            for (BigInteger i = startBlock; i.compareTo(latestBlock) <= 0; i = i.add(BigInteger.ONE)) {
                EthBlock block = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), true).send();
                List<EthBlock.TransactionResult> transactions = block.getBlock().getTransactions();

                for (EthBlock.TransactionResult txResult : transactions) {
                    Transaction tx = (Transaction) txResult.get();

                    if (tx.getTo() == null) continue; // 合约创建等交易过滤掉

                    if (merchantAddress.equals(tx.getTo().toLowerCase())) {
                        // 找待支付订单（无 txHash）匹配付款地址和金额
                        List<PaySeqEntity> candidates = paySeqMapper.selectList(new LambdaQueryWrapper<PaySeqEntity>().isNull(PaySeqEntity::getThirdIdentify));

                        for (PaySeqEntity order : candidates) {
                            // 地址匹配
                            if (!order.getPayAccount().equalsIgnoreCase(tx.getFrom())) continue;

                            // 金额匹配（wei精度比对）
                            BigDecimal txValueEth = new BigDecimal(tx.getValue());
                            BigDecimal orderValueWei = order.getPayAmount().multiply(BigDecimal.TEN.pow(18));

                            if (txValueEth.compareTo(orderValueWei) == 0) {
                                // 查询交易是否被确认（可选）
                                EthGetTransactionReceipt receiptOpt = web3j.ethGetTransactionReceipt(tx.getHash()).sendAsync().get();
                                if (receiptOpt != null) {
                                    // 更新订单
                                    order.setThirdIdentify(tx.getHash());
                                    order.setStatus(PayOrderStatusEnum.PAY_SUCCESS.getValue());
                                    paySeqMapper.insertOrUpdate(order);
                                    System.out.println("订单 " + order.getPaySeq()+ " 匹配到链上交易：" + tx.getHash());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
