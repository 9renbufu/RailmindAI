package com.railmind.message;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.railmind.message", "com.railmind.common"})
public class MessageApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageApplication.class, args);
    }
}
