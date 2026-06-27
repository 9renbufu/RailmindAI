package com.railmind.train;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.railmind.train", "com.railmind.common"})
public class TrainApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrainApplication.class, args);
    }
}
