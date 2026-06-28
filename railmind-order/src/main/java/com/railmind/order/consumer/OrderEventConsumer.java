package com.railmind.order.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventConsumer {

    @KafkaListener(topics = "order-created", groupId = "order-group")
    public void onOrderCreated(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String orderNo = record.key();
        String payload = record.value();
        log.info("收到订单创建事件: orderNo={}, partition={}, offset={}",
                orderNo, record.partition(), record.offset());

        try {
            // TODO: 后续由支付服务消费此事件创建支付单
            // 当前仅记录日志，确认消息已收到
            log.info("订单创建事件处理完成: orderNo={}", orderNo);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("订单创建事件处理失败: orderNo={}", orderNo, e);
            // 不确认，让Kafka重试
        }
    }
}
