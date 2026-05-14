package com.demo.accountapi.exposition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.demo.accountapi")
@EnableJpaRepositories(basePackages = "com.demo.accountapi.infrastructure.persistence.repository")
@EntityScan(basePackages = "com.demo.accountapi.infrastructure.persistence.entity")
public class PaymentAccountApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentAccountApplication.class, args);
    }
}
