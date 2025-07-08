package com.payment.conroller;


import com.payment.model.dto.PayDTO;
import com.payment.model.dto.PayResultDTO;
import com.payment.model.entity.PaySeqEntity;
import com.payment.service.PayTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order/")
public class OrderController {

    @Autowired
    private PayTemplate payTemplate;

    @RequestMapping("/create")
    @ResponseBody
    public PayResultDTO createOrder(@RequestBody PayDTO payDTO) {
       return payTemplate.toPay(payDTO);
    }

    @RequestMapping("/updateTxHash")
    @ResponseBody
    public void uodtateOrder(@RequestBody PaySeqEntity paySeq) {
        payTemplate.updateTxtHash(paySeq);
    }
}
