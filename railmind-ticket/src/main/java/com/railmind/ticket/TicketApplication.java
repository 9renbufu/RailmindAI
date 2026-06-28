package com.railmind.ticket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.railmind.ticket", "com.railmind.common"})
@MapperScan({"com.railmind.ticket.mapper", "com.railmind.train.mapper"})
public class TicketApplication {
    public static void main(String[] args) {
        SpringApplication.run(TicketApplication.class, args);
    }
}
