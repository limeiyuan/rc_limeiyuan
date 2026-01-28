package com.example.adapter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.adapter.repository")
public class HttpAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(HttpAdapterApplication.class, args);
    }
}
