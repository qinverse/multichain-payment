package com.payment.component.chain;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.payment.common.base.PayOrderStatusEnum;
import com.payment.component.IPayStrategy;
import com.payment.model.dto.*;
import com.payment.model.entity.PaySeqEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Service("eth")
@Slf4j
public class EthChainTransactionComponent implements IPayStrategy {

    private final Map<String, Web3j> chainWeb3Map = new HashMap<>();


    private final Cache<BigInteger, LocalDateTime> blockTimeCache = Caffeine.newBuilder()
            .maximumSize(300)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();


    public EthChainTransactionComponent() {
        chainWeb3Map.put("eth", Web3j.build(new HttpService("https://sepolia.infura.io/v3/23599b1b46fa40c99a81c4a376c8fdba")));
        chainWeb3Map.put("bsc", Web3j.build(new HttpService("https://bsc-testnet.infura.io/v3/23599b1b46fa40c99a81c4a376c8fdba")));
        chainWeb3Map.put("polygon", Web3j.build(new HttpService("https://polygon-amoy.infura.io/v3/23599b1b46fa40c99a81c4a376c8fdba")));
        chainWeb3Map.put("arbitrum", Web3j.build(new HttpService("https://arbitrum-sepolia.infura.io/v3/23599b1b46fa40c99a81c4a376c8fdba")));
    }

    public String sendTransaction(String chain, String privateKey, String toAddress, BigDecimal amount) throws Exception {
        Web3j web3 = chainWeb3Map.get(chain.toLowerCase());
        if (web3 == null) throw new IllegalArgumentException("Unsupported chain");
        Credentials credentials = Credentials.create(privateKey);
        TransactionReceipt receipt = Transfer.sendFunds(
                web3, credentials, toAddress, amount, Convert.Unit.ETHER).send();
        return receipt.getTransactionHash();
    }

    public Web3j getWeb3(String chain) {
        Web3j web3 = chainWeb3Map.get(chain.toLowerCase());
        if (web3 == null) throw new IllegalArgumentException("Unsupported chain");
        return web3;
    }

    /**
     * 前端钱包直连链上
     * txHash 由前端回传
     */
    @Override
    public PayRequestResultDTO doPay(PayDTO payDTo) {
        return null;
    }

    @Override
    public PayResultDTO queryPay(PayQueryDTO payQueryDTO) {

        PayResultDTO dto = new PayResultDTO();

        try {
            Web3j web3 = chainWeb3Map.get(
                    payQueryDTO.getChain().toLowerCase()
            );

            EthGetTransactionReceipt ethReceipt =
                    web3.ethGetTransactionReceipt(
                            payQueryDTO.getThirdIdentify()
                    ).send();

            // 1️⃣ 还没上链
            if (!ethReceipt.getTransactionReceipt().isPresent()) {
                dto.setStatus(PayOrderStatusEnum.PAY_TX_SUBMITTED.getValue());
                dto.setMsg("交易未上链");
                return dto;
            }

            TransactionReceipt receipt =
                    ethReceipt.getTransactionReceipt().get();

            // 2️⃣ 执行失败（revert）
            if (!receipt.isStatusOK()) {
                dto.setStatus(PayOrderStatusEnum.PAY_FAIL.getValue());
                dto.setMsg("交易执行失败");
                return dto;
            }

            // 3️⃣ 确认数检查
            BigInteger blockNumber = receipt.getBlockNumber();
            BigInteger currentBlock =
                    web3.ethBlockNumber().send().getBlockNumber();

            int confirms = currentBlock.subtract(blockNumber).intValue();

            if (confirms < 12) {
                dto.setStatus(PayOrderStatusEnum.PAY_CONFIRMING.getValue());
                dto.setMsg("链上确认中，当前确认数：" + confirms);
                return dto;
            }

            // 4️⃣ 终态成功 → 采集链上事实
            dto.setStatus(PayOrderStatusEnum.PAY_SUCCESS.getValue());

            // === 区块高度 ===
            dto.setBlockNumber(blockNumber.longValue());

            // === 区块确认时间 ===
            EthBlock ethBlock = web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), false).send();

            BigInteger ts = ethBlock.getBlock().getTimestamp();

            LocalDateTime confirmedAt =
                    LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(ts.longValue()),
                            ZoneId.systemDefault()
                    );

            dto.setConfirmedAt(confirmedAt);

            // === 实际手续费 ===
            dto.setFee(calcFee(receipt));

            return dto;

        } catch (Exception e) {
            dto.setStatus(PayOrderStatusEnum.PAY_ABNORMAL.getValue());
            dto.setMsg("链上查询异常：" + e.getMessage());
            return dto;
        }
    }


    @Override
    public <T> PayResultDTO payNotify(T notify) {
        return null;
    }

    public Optional<TxScanResultDTO> scanFinalTx(PaySeqEntity order) {

        try {
            Web3j web3 = getWeb3(order.getType());

            BigInteger latestBlock = web3.ethBlockNumber().send().getBlockNumber();

            // 兜底：从最近 N 个区块开始扫
            // 一般 3000~6000 个区块（ETH ≈ 12~24 小时）
            BigInteger fromBlock = latestBlock.subtract(BigInteger.valueOf(6000));
            if (fromBlock.compareTo(BigInteger.ZERO) < 0) {
                fromBlock = BigInteger.ZERO;
            }

            for (BigInteger i = fromBlock; i.compareTo(latestBlock) <= 0; i = i.add(BigInteger.ONE)) {

                EthBlock block = web3.ethGetBlockByNumber(
                        DefaultBlockParameter.valueOf(i),
                        true
                ).send();
                BigInteger blockTimestamp = block.getBlock().getTimestamp();

                for (EthBlock.TransactionResult<?> txr : block.getBlock().getTransactions()) {

                    EthBlock.TransactionObject tx = (EthBlock.TransactionObject) txr.get();

                    if (match(tx, blockTimestamp, order)) {

                        EthGetTransactionReceipt receiptResp = web3.ethGetTransactionReceipt(tx.getHash()).send();

                        if (!receiptResp.getTransactionReceipt().isPresent()) {
                            continue;
                        }

                        TransactionReceipt receipt = receiptResp.getTransactionReceipt().get();
                        TxScanResultDTO result = new TxScanResultDTO();
                        result.setFinalTxHash(tx.getHash());
                        result.setNonce(tx.getNonce().longValue());
                        result.setBlockNumber(receipt.getBlockNumber().longValue());
                        result.setFee(calcFee(receipt));
                        result.setConfirmedAt(getConfirmedTime(web3, receipt));
                        return Optional.of(result);
                    }
                }
            }
        } catch (Exception e) {
            log.error("scanFinalTx error, paySeq={}", order.getPaySeq(), e);
        }

        return Optional.empty();
    }

    private boolean match(EthBlock.TransactionObject tx, BigInteger blockTimestamp, PaySeqEntity order) {

        // 1. to 地址
        if (tx.getTo() == null ||
                !tx.getTo().equalsIgnoreCase(order.getReceiveAccount())) {
            return false;
        }

        // 2. from knowing（强烈推荐）
        if (order.getPayAccount() != null &&
                !tx.getFrom().equalsIgnoreCase(order.getPayAccount())) {
            return false;
        }

        // 3. 金额
        BigDecimal txValue = Convert.fromWei(
                new BigDecimal(tx.getValue()),
                Convert.Unit.ETHER
        );

        if (txValue.compareTo(order.getPayAmount()) != 0) {
            return false;
        }

        // 4. 时间窗口（核心兜底）
        if (order.getCreatedTime() != null) {
            long txTime = blockTimestamp.longValue() * 1000L;
            long createTime =
                    order.getCreatedTime()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli();

            // ±10 分钟
            if (Math.abs(txTime - createTime) > 10 * 60 * 1000) {
                return false;
            }
        }

        return true;
    }


    public static BigDecimal calcFee(TransactionReceipt receipt) {

        BigInteger gasUsed = receipt.getGasUsed();
        BigInteger gasPrice = Numeric.decodeQuantity(receipt.getEffectiveGasPrice());

        BigInteger feeWei = gasUsed.multiply(gasPrice);

        return Convert.fromWei(
                new BigDecimal(feeWei),
                Convert.Unit.ETHER
        );
    }


    private LocalDateTime getConfirmedTime(Web3j web3, TransactionReceipt receipt) {
        try {
            return blockTimeCache.get(
                    receipt.getBlockNumber(),
                    bn -> fetchBlockTime(web3, bn)
            );
        } catch (Exception e) {
            log.error("获取区块时间异常 block={}", receipt.getBlockNumber(), e);
            return null;
        }
    }

    private LocalDateTime fetchBlockTime(Web3j web3, BigInteger blockNumber) {
        try {
            EthBlock block = web3.ethGetBlockByNumber(
                    DefaultBlockParameter.valueOf(blockNumber),
                    false
            ).send();

            BigInteger ts = block.getBlock().getTimestamp();
            return LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(ts.longValue()),
                    ZoneId.systemDefault()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}