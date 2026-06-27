# RailMind AI - 智能铁路售票系统

## 项目概述

企业级铁路售票系统，具备 AI 智能能力。对标 12306，集成 Spring AI 实现智能客服、路线推荐、客流预测等功能。

## 技术栈

- Java 21 + Spring Boot 3.3.x + Spring AI 1.0.x
- Spring Security 6.x + JWT (jjwt 0.12.x)
- Spring Data JPA (主) + MyBatis Plus (备用)
- Redis 7.x (缓存/分布式锁) + Redisson
- Apache Kafka 3.x (异步消息)
- MySQL 8.0 (主数据库)
- Spring Cloud Gateway 4.x (API网关)
- Sentinel 1.8.x (限流熔断)
- Caffeine 3.x (本地缓存)
- Docker / Docker Compose
- Maven 多模块
- Vue 3 + Vite + Element Plus + Pinia (前端)

## 架构风格

- DDD 分层架构 (controller → service → domain → repository)
- 模块化单体，可渐进拆分微服务
- 事件驱动 (Kafka) + 同步调用 (OpenFeign)
- 三级缓存 (Caffeine → Redis → MySQL)
- Redis + Lua 原子扣减 + Kafka 异步下单

## 模块结构

```
railmind-common     公共模块 (工具/异常/常量/配置)
railmind-gateway    API网关 (8080)
railmind-user       用户服务 (8081)
railmind-train      车次服务 (8082)
railmind-ticket     票务服务 (8083) - 核心高频
railmind-order      订单服务 (8084)
railmind-payment    支付服务 (8085)
railmind-message    消息服务 (8086) - WebSocket
railmind-ai         AI服务 (8087) - Spring AI
railmind-admin      管理后台 (8088)
```

## 命名规范

- 包名: `com.railmind.{module}`
- 实体: `t_` 表前缀，JPA Entity 无前缀
- DTO: `*Request` / `*Response` / `*DTO`
- 服务接口: `*Service`，实现: `*ServiceImpl`
- 领域服务: `*DomainService`
- 仓库: `*Repository` (JPA) / `*Mapper` (MyBatis)
- Kafka 消费者: `*Consumer`
- Kafka 生产者: `*Producer`
- 事件: `*Event`

## 编码规范

- 统一返回: `Result<T>` 包装
- 全局异常: `BizException` + `ErrorCode` 枚举
- ID 生成: 雪花算法 (`IdGenerator`)
- 敏感数据: AES 加密存储 (身份证号等)
- 密码: BCrypt 加密
- 逻辑删除: `deleted` 字段 (0/1)
- 乐观锁: `version` 字段

## 数据库规范

- 字符集: utf8mb4
- 引擎: InnoDB
- 时间字段: `created_at` / `updated_at` (DATETIME)
- 索引命名: `idx_` 前缀
- 唯一约束: `uk_` 前缀

## Redis Key 规范

```
{业务}:{模块}:{维度}:{标识}

user:token:{accessToken}       → userId (15min)
ticket:stock:{trainId}:{date}:{from}:{to}:{seatType} → 数量 (30s)
seat:lock:{trainId}:{date}:{seatNo} → orderNo (15min)
order:timeout:{orderNo}        → "" (15min, TTL驱动超时)
waitlist:queue:{trainId}:{date}:{from}:{to}:{seatType} → ZSet (24h)
```

## 核心业务流程

### 购票
查询余票 → 选择车次 → 选乘车人/座位 → Redis Lua锁库存 → Kafka异步创建订单 → 支付 → 出票

### 退票
选择订单 → 计算退票费 → 退款 → 释放库存 → Kafka通知候补

### 候补
余票不足 → Redis ZSet入队 → 有退票时自动兑现 → 通知支付

## AI 能力

- 智能客服: Spring AI + RAG (向量检索 + LLM)
- 路线推荐: Function Calling (searchTrains/getPrice/getPreference)
- 客流预测: LLM + 历史数据分析
- 异常检测: 规则引擎 + LLM 双重检测

## 开发里程碑

M1: 项目骨架 + 公共模块 + 用户服务 (1周)
M2: 车次服务 + 站点数据 (1周)
M3: 票务服务 - 余票查询/库存扣减 (1.5周)
M4: 订单服务 - 下单/支付/退改签 (1.5周)
M5: 消息服务 - WebSocket (0.5周)
M6: AI服务 - 客服/推荐 (1周)
M7: 管理后台 (1周)
M8: 高并发优化/压测 (1周)
M9: Vue3前端 (1.5周)
M10: Docker部署/文档 (0.5周)

## 设计文档

详细设计文档位于 `docs/design/` 目录:
- 01-系统需求分析.md
- 02-功能模块划分.md
- 03-微服务划分.md
- 04-数据流设计.md
- 05-数据库设计.md
- 06-Redis设计.md
- 07-Kafka消息流设计.md
- 08-高并发方案.md
- 09-AI能力设计.md
- 10-项目目录结构.md

## 环境要求

- JDK 21
- Maven 3.9+
- Docker + Docker Compose
- Node.js 18+ (前端)
