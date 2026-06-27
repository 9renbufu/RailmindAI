# RailMind AI - Kafka 消息流设计

> 版本：v1.0 | 日期：2026-06-27

---

## 一、Topic 规划

| Topic | Partitions | Replication | 消费者组 | 说明 |
|-------|-----------|-------------|---------|------|
| `order-created` | 16 | 3 | payment-group, notify-group | 订单创建事件 |
| `order-paid` | 16 | 3 | ticket-group, notify-group | 订单支付成功 |
| `order-cancelled` | 16 | 3 | ticket-group, waitlist-group | 订单取消 |
| `order-refunded` | 8 | 3 | notify-group | 退款完成 |
| `ticket-locked` | 16 | 3 | order-group | 库存锁定成功 |
| `ticket-released` | 16 | 3 | waitlist-group | 库存释放 |
| `waitlist-fulfilled` | 8 | 3 | notify-group | 候补兑现 |
| `train-delayed` | 4 | 3 | notify-group | 列车晚点通知 |
| `ai-event` | 8 | 3 | ai-group | AI分析事件（用户行为采集） |

---

## 二、消息体设计

### 2.1 订单创建事件

```java
public record OrderCreatedEvent(
    String orderNo,
    Long userId,
    Long trainId,
    String trainNo,
    LocalDate travelDate,
    String fromStation,
    String toStation,
    List<OrderItemDTO> items,
    BigDecimal totalAmount,
    LocalDateTime payDeadline,
    LocalDateTime createdAt
) {}
```

### 2.2 库存释放事件

```java
public record TicketReleasedEvent(
    Long trainId,
    LocalDate travelDate,
    String fromStation,
    String toStation,
    String seatTypeCode,
    int count,
    String orderNo,
    LocalDateTime releasedAt
) {}
```

### 2.3 支付结果事件

```java
public record PaymentResultEvent(
    String paymentNo,
    String orderNo,
    Long userId,
    BigDecimal amount,
    String payType,
    String status,  // SUCCESS / FAILED
    LocalDateTime paidAt
) {}
```

### 2.4 候补兑现事件

```java
public record WaitlistFulfilledEvent(
    Long waitlistId,
    Long userId,
    String orderNo,
    Long trainId,
    LocalDate travelDate,
    LocalDateTime fulfilledAt
) {}
```

---

## 三、消息可靠性保障

| 环节 | 策略 |
|------|------|
| 生产端 | `acks=all` + 重试3次 + 本地消息表兜底 |
| Broker | 多副本 + `min.insync.replicas=2` |
| 消费端 | 手动ACK + 幂等消费（消息ID去重表） |
| 顺序性 | 同一订单的消息发往同一Partition（orderNo hash） |
| 死信队列 | 消费失败3次进入死信Topic，人工处理 |

---

## 四、消息流程图

```
┌──────────────┐                   ┌──────────────────────────────┐
│ Order Service│──order-created──> │ Payment Service (扣款)        │
│              │                   │ Message Service (推送)        │
│ Ticket Service│──ticket-locked─> │ Order Service (确认订单)      │
│              │                   │                              │
│ Payment Svc  │──order-paid────> │ Ticket Service (确认出票)     │
│              │                   │ Message Service (通知)        │
│              │                   │ AI Service (行为分析)         │
└──────────────┘                   └──────────────────────────────┘
```

---

## 五、消费者示例结构

```
consumer/
├── OrderEventConsumer.java        // ticket服务：消费订单创建 → 扣库存
├── PaymentResultConsumer.java     // order服务：消费支付结果 → 更新订单状态
├── TicketEventConsumer.java       // order服务：消费库存锁定 → 确认订单
├── OrderEventNotifyConsumer.java  // message服务：消费订单事件 → WebSocket推送
├── TicketEventNotifyConsumer.java // message服务：消费余票事件 → 通知候补
└── AiEventConsumer.java           // ai服务：消费用户行为 → 分析建模
```
