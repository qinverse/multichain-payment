package com.payment;

import jakarta.annotation.PostConstruct;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * @author qinverse
 * @date 2025/6/23 8:37
 * @description 启动类
 */
@SpringBootApplication
@MapperScan(basePackages = "com.payment.mapper")
public class MultichainPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultichainPaymentApplication.class, args);
    }

    @PostConstruct
    public void init() {
        System.out.println("当前使用的数据源 URL: " + System.getProperty("spring.datasource.url"));
    }
}
