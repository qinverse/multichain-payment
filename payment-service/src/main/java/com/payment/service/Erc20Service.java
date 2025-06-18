package com.payment.service;

import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

@Service
public class Erc20Service {

    private final MultichainTransactionService chainService;

    public Erc20Service(MultichainTransactionService chainService) {
        this.chainService = chainService;
    }

    /**
     * 转账 ERC20 代币
     *
     * @param chain 区块链标识，如 eth、bsc、polygon
     * @param privateKey 钱包私钥
     * @param contractAddress 代币合约地址
     * @param toAddress 目标地址
     * @param amount 转账数量（人类可读单位，自动转换为最小单位）
     * @return 交易哈希
     * @throws Exception 异常信息
     */
    public String transferToken(String chain, String privateKey, String contractAddress, String toAddress, BigDecimal amount) throws Exception {
        Web3j web3j = chainService.getWeb3(chain);
        if (web3j == null) {
            throw new IllegalArgumentException("Unsupported chain: " + chain);
        }

        Credentials credentials = Credentials.create(privateKey);
        ContractGasProvider gasProvider = new DefaultGasProvider();

        // ERC20 的 transfer 方法需要的参数类型
        Function function = new Function(
                "transfer",
                Arrays.asList(new Address(toAddress), new Uint256(amount.multiply(BigDecimal.TEN.pow(18)).toBigInteger())),
                Collections.singletonList(new TypeReference<Type>() {})
        );

        String encodedFunction = FunctionEncoder.encode(function);

        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials);
        EthSendTransaction transactionResponse = txManager.sendTransaction(
                gasProvider.getGasPrice(contractAddress),
                gasProvider.getGasLimit(contractAddress),
                contractAddress,
                encodedFunction,
                BigInteger.ZERO
        );

        if (transactionResponse.hasError()) {
            throw new RuntimeException("Token transfer failed: " + transactionResponse.getError().getMessage());
        }

        return transactionResponse.getTransactionHash();
    }
}