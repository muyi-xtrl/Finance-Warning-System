package com.finance.warning;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.finance.warning.mapper")
public class FinanceWarningApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceWarningApplication.class, args);
    }
}