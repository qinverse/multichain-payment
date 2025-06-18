package com.payment.conroller;

import com.wallet.service.MultichainTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private MultichainTransactionService transactionService;

    @PostMapping("/transfer")
    public String transfer(@RequestParam String chain,
                           @RequestParam String privateKey,
                           @RequestParam String to,
                           @RequestParam BigDecimal amount) throws Exception {
        return transactionService.sendTransaction(chain, privateKey, to, amount);
    }
}