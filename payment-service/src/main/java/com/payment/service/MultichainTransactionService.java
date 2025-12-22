package com.payment.service;

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
public class MultichainTransactionService {

    private final Map<String, Web3j> chainWeb3Map = new HashMap<>();


    public MultichainTransactionService() {
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
}