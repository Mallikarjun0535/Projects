package com.rsystems.vehiclesales;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.rsystems.vehiclesales.dao")
public class VechileSalesApplication {

    public static void main(String[] args) {
        SpringApplication.run(VechileSalesApplication.class, args);
    }
}
