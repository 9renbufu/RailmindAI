package com.railmind.ticket.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.railmind.ticket.vo.TicketQueryVO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineCacheConfig {

    @Bean
    public Cache<String, TicketQueryVO> ticketQueryLocalCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .recordStats()
                .build();
    }

    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}
