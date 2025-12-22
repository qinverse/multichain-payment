package com.payment.conroller;

import com.payment.model.dto.PayDTO;
import com.payment.model.dto.PayResultDTO;
import com.payment.service.PayTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import com.payment.service.MultichainTransactionService;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

   /* @Autowired
    private MultichainTransactionService transactionService;*/

    @Autowired
    private PayTemplate payTemplate;

    //    @PostMapping("/transfer")
    public String transfer(@RequestParam String chain,
                           @RequestParam String privateKey,
                           @RequestParam String to,
                           @RequestParam BigDecimal amount) throws Exception {
//        return transactionService.sendTransaction(chain, privateKey, to, amount);
        return null;
    }

    @PostMapping("/transfer")
    public PayResultDTO transfer(@ModelAttribute PayDTO dto) throws Exception {
        return payTemplate.toPay(dto);
    }
}