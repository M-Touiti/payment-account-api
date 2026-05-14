package com.demo.accountapi.exposition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.demo.accountapi")
public class PaymentAccountApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentAccountApplication.class, args);
    }
}
