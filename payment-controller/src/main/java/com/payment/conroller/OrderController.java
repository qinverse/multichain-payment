package com.payment.conroller;


import com.payment.common.result.ResultDTO;
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

    /**
     * 做成配置 testdemo1地址
     */
    private String toAddress = "0x2e13e7c90b6d627dbb06768c8018a4fbf030ac9c";

    @RequestMapping("/create")
    @ResponseBody
    public ResultDTO<PayResultDTO> createOrder(@RequestBody PayDTO payDTO) {
        payDTO.setToAddress(toAddress);
       return ResultDTO.success(payTemplate.toPay(payDTO));
    }

    @RequestMapping("/updateTxHash")
    @ResponseBody
    public ResultDTO<Void> uodtateOrder(@RequestBody PaySeqEntity paySeq) {
        payTemplate.updateTxtHash(paySeq);
        return ResultDTO.success();
    }

    @RequestMapping("/refreshOrder")
    @ResponseBody
    public ResultDTO<Void> refreshOrder(String paySeq) {
        payTemplate.refresOrder(paySeq);
        return ResultDTO.success();
    }
}
