package com.component.chain;

import com.component.PayStrategy;
import com.payment.common.base.PayOrderStatusEnum;
import com.payment.model.dto.PayDTO;
import com.payment.model.dto.PayQueryDTO;
import com.payment.model.dto.PayRequestResultDTO;
import com.payment.model.dto.PayResultDTO;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ChainTransactionComponent implements PayStrategy {

    private final Map<String, Web3j> chainWeb3Map = new HashMap<>();


    public ChainTransactionComponent() {
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
        try {
            Web3j web3 = chainWeb3Map.get(payDTo.getChain().toLowerCase());
            if (web3 == null) throw new IllegalArgumentException("Unsupported chain");

            Credentials credentials = Credentials.create(payDTo.getPrivateKey());
            TransactionReceipt receipt = Transfer.sendFunds(
                    web3, credentials, payDTo.getToAddress(), payDTo.getAmount(), Convert.Unit.ETHER).send();
            dto.setStatus(PayOrderStatusEnum.PAY_PENDING.getValue());
            dto.setThirdIdentify(receipt.getTransactionHash());
            dto.setFee(new BigDecimal(receipt.getEffectiveGasPrice()));
            return dto;
        } catch (Exception e) {
            dto.setStatus(PayOrderStatusEnum.PAY_PENDING.getValue());
            dto.setMsg("链上转账异常，支付结果未知");
            return dto;
        }
    }

    @Override
    public PayResultDTO queryPay(PayQueryDTO payQueryDTO) {
        PayResultDTO dto = new PayResultDTO();
        try {
            Web3j web3 = chainWeb3Map.get(payQueryDTO.getChain().toLowerCase());
            if (web3 == null) throw new IllegalArgumentException("Unsupported chain");


            TransactionReceipt receipt = Transfer.sendFunds(
                    web3, credentials, payDTo.getToAddress(), payDTo.getAmount(), Convert.Unit.ETHER).send();
            dto.setStatus(PayOrderStatusEnum.PAY_PENDING.getValue());
            dto.setThirdIdentify(receipt.getTransactionHash());
            dto.setFee(new BigDecimal(receipt.getEffectiveGasPrice()));
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