package com.payment.conroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallet")
public class TokenTransferController {

    @Autowired
    private Erc20Service erc20Service;

    /**
     * ERC20 代币转账接口
     *
     * @param chain 区块链标识（eth、bsc、polygon、arbitrum）
     * @param privateKey 钱包私钥
     * @param contractAddress 代币合约地址
     * @param to 接收地址
     * @param amount 转账数量（单位：代币数量）
     * @return 交易哈希
     * @throws Exception 异常信息
     */
    @PostMapping("/token-transfer")
    public String transferToken(@RequestParam String chain,
                                @RequestParam String privateKey,
                                @RequestParam String contractAddress,
                                @RequestParam String to,
                                @RequestParam BigDecimal amount) throws Exception {
        return erc20Service.transferToken(chain, privateKey, contractAddress, to, amount);
    }
}