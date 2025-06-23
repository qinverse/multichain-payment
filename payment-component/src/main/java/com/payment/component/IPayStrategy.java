package com.payment.component;


import com.payment.model.dto.PayDTO;
import com.payment.model.dto.PayQueryDTO;
import com.payment.model.dto.PayRequestResultDTO;
import com.payment.model.dto.PayResultDTO;

public interface IPayStrategy {

    PayRequestResultDTO doPay(PayDTO payDTo);

    PayResultDTO queryPay(PayQueryDTO payQueryDTO);

    <T> PayResultDTO payNotify(T notify);
}
