package com.railmind.order.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.railmind.order.domain.event.OrderCreatedEvent;
import com.railmind.order.domain.model.Outbox;
import com.railmind.order.mapper.OutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String TOPIC_ORDER_CREATED = "order-created";
    private static final String AGGREGATE_TYPE = "ORDER";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxMapper outboxMapper;
    private final ObjectMapper objectMapper;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("序列化订单事件失败: orderNo={}", event.getOrderNo(), e);
            throw new RuntimeException("序列化订单事件失败", e);
        }

        Outbox outbox = Outbox.builder()
                .aggregateType(AGGREGATE_TYPE)
                .aggregateId(event.getOrderNo())
                .eventType("OrderCreatedEvent")
                .payload(payload)
                .status("PENDING")
                .retryCount(0)
                .build();
        outboxMapper.insert(outbox);
        log.info("订单事件已写入Outbox: orderNo={}, outboxId={}", event.getOrderNo(), outbox.getId());

        try {
            kafkaTemplate.send(TOPIC_ORDER_CREATED, event.getOrderNo(), payload)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            outboxMapper.markSent(outbox.getId());
                            log.info("订单事件发送成功: orderNo={}", event.getOrderNo());
                        } else {
                            outboxMapper.markFailed(outbox.getId(), ex.getMessage());
                            log.error("订单事件发送失败: orderNo={}", event.getOrderNo(), ex);
                        }
                    });
        } catch (Exception e) {
            outboxMapper.markFailed(outbox.getId(), e.getMessage());
            log.error("订单事件发送异常: orderNo={}", event.getOrderNo(), e);
        }
    }
}
