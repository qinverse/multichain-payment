package com.payment.service;

import com.payment.model.dto.PayDTO;
import org.springframework.stereotype.Service;

@Service
public class PayTemplate extends AbstractPayTemplate {
    @Override
    void checkParam(PayDTO dto) {

    }
}
