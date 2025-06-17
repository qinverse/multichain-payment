package com.wallet.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class ListenServer {

    private Map<String, Web3j> chainWeb3Map = Map.of(
            "eth", Web3j.build(new HttpService("https://sepolia.infura.io/v3/23599b1b46fa40c99a81c4a376c8fdba"))
//            ,"bsc", Web3j.build(new HttpService("https://bsc-testnet.infura.io/v3/23599b1b46fa40c99a81c4a376c8fdba"))
           );



    // 代币配置：链 -> 合约地址列表
    private Map<String, List<String>> tokenMap = Map.of(
            "eth", List.of("0xe3a6E3935E65613C7DE0DB4586dcc91a32A03c41",
                    "0x2e13e7C90B6d627DbB06768c8018A4fbF030AC9c") // USDC on ETH
//            ,"bsc", List.of("0x55d398326f99059fF775485246999027B3197955") // USDT on BSC);
    );


    @PostConstruct
    public void init() {
        // ERC20 Transfer 事件
        Event transferEvent = new Event("Transfer",
                Arrays.asList(
                        new TypeReference<Address>(true) {
                        },  // from
                        new TypeReference<Address>(true) {
                        },  // to
                        new TypeReference<Uint256>() {
                        }       // value
                )
        );

        String transferTopic = EventEncoder.encode(transferEvent);

        // 对每条链的每个代币合约设置监听
        for (Map.Entry<String, List<String>> entry : tokenMap.entrySet()) {
            String chain = entry.getKey();
            Web3j web3j = chainWeb3Map.get(chain);
            if (web3j == null) continue;

            for (String contract : entry.getValue()) {
                EthFilter filter = new EthFilter(
                        DefaultBlockParameterName.LATEST,
                        DefaultBlockParameterName.LATEST,
                        contract
                );
                filter.addSingleTopic(transferTopic);

                web3j.ethLogFlowable(filter).subscribe(log -> {
                    EventValues eventValues = Contract.staticExtractEventParameters(transferEvent, log);
                    String from = eventValues.getIndexedValues().get(0).getValue().toString();
                    String to = eventValues.getIndexedValues().get(1).getValue().toString();
                    BigInteger rawValue = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                    BigDecimal value = new BigDecimal(rawValue).divide(BigDecimal.TEN.pow(18));

                    System.out.printf("链: %s, 代币: %s, 从: %s -> 到: %s, 数量: %s%n",
                            chain, contract, from, to, value.toPlainString());
                });
            }
        }
    }

}
