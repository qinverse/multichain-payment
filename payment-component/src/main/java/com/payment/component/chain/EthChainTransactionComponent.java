package com.payment.component.chain;

import com.alibaba.fastjson.JSONObject;
import com.payment.component.IPayStrategy;
import com.payment.common.base.PayOrderStatusEnum;
import com.payment.model.dto.PayDTO;
import com.payment.model.dto.PayQueryDTO;
import com.payment.model.dto.PayRequestResultDTO;
import com.payment.model.dto.PayResultDTO;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;


@Service("eth")
public class EthChainTransactionComponent implements IPayStrategy {

    private final Map<String, Web3j> chainWeb3Map = new HashMap<>();

    /**
     * 做成配置
     */
    private String toAddress = "0xe3a6E3935E65613C7DE0DB4586dcc91a32A03c41";


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

    @Override
    public PayRequestResultDTO doPay(PayDTO payDTo) {
        PayRequestResultDTO dto = new PayRequestResultDTO();
        dto.setStatus(PayOrderStatusEnum.PAY_PENDING.getValue());
            dto.setMsg("链上转账异常，支付结果未知");
            dto.setToAddress(toAddress);
            return dto;
//        try {
//            Web3j web3 = chainWeb3Map.get(payDTo.getChannelCode().toLowerCase());
//            if (web3 == null) throw new IllegalArgumentException("Unsupported chain");
//
//            Credentials credentials = Credentials.create(payDTo.getPrivateKey());
//            TransactionReceipt receipt = Transfer.sendFunds(
//                    web3, credentials, payDTo.getToAddress(), payDTo.getAmount(), Convert.Unit.ETHER).send();
//            dto.setStatus(PayOrderStatusEnum.PAY_LISTENLING.getValue());
//            dto.setThirdIdentify(receipt.getTransactionHash());
//            dto.setFee(Convert.fromWei(String.valueOf(receipt.getGasUsed().intValue()), Convert.Unit.WEI));
//            return dto;
//        } catch (Exception e) {
//            dto.setStatus(PayOrderStatusEnum.PAY_PENDING.getValue());
//            dto.setMsg("链上转账异常，支付结果未知");
//            return dto;
//        }
    }

    @Override
    public PayResultDTO queryPay(PayQueryDTO payQueryDTO) {
        PayResultDTO dto = new PayResultDTO();
        try {
            Web3j web3 = chainWeb3Map.get(payQueryDTO.getChain().toLowerCase());
            if (web3 == null) throw new IllegalArgumentException("Unsupported chain");

            EthGetTransactionReceipt transactionReceipt = web3.ethGetTransactionReceipt(payQueryDTO.getThirdIdentify()).send();
             if (!transactionReceipt.getTransactionReceipt().isPresent()) {
                 dto.setStatus(PayOrderStatusEnum.PAY_PENDING.getValue());
                 dto.setMsg("未查询到结果，支付结果未知");
                 return dto;
             }
            TransactionReceipt receipt = transactionReceipt.getTransactionReceipt().get();
             if (!receipt.isStatusOK()) {
                 dto.setStatus(PayOrderStatusEnum.PAY_FAIL.getValue());
                 dto.setMsg(JSONObject.toJSONString(receipt.getLogs()));
                 return dto;
             }
            dto.setStatus(PayOrderStatusEnum.PAY_SUCCESS.getValue());
            BigInteger gasUsed = receipt.getGasUsed();
            BigInteger effectiveGasPrice = Numeric.decodeQuantity(receipt.getEffectiveGasPrice()); // or receipt.getEffectiveGasPrice() if it's already BigInteger
            BigInteger totalFeeInWei = gasUsed.multiply(effectiveGasPrice);

            // 转成 ETH
            BigDecimal feeEth = new BigDecimal(totalFeeInWei).divide(BigDecimal.TEN.pow(18), 18, RoundingMode.HALF_UP);
            System.out.println("实际手续费: " + feeEth + " ETH");

            dto.setFee(feeEth);
            return dto;
        } catch (Exception e) {
            dto.setStatus(PayOrderStatusEnum.PAY_PENDING.getValue());
            dto.setMsg("链上转账异常，支付结果未知");
            return dto;
        }
    }

    @Override
    public <T> PayResultDTO payNotify(T notify) {
        return null;
    }
}