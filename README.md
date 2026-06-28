# RailMind AI - AI 驱动的智能铁路售票平台

> **AI 不是附属功能，而是贯穿购票、改签、候补、出行规划、客服、运维全链路的核心业务智能层。**

---

## 项目定位

RailMind AI 是一个面向铁路售票场景的企业级平台，核心解决两个问题：

1. **高并发**: 余票查询 50,000 QPS、下单 5,000 QPS，三级缓存 + Redis Lua 原子扣减 + Kafka 异步解耦
2. **智能化**: 6 个 AI Agent 深度融入业务决策，不是聊天机器人，而是自主执行的业务大脑

```
传统模式:  用户 → 查票 → 下单 → 支付 → (有问题找AI客服)
                AI 是被动响应的附属窗口

RailMind:  用户 → 描述需求 → AI Agent 自主决策 → 执行 → 反馈
                AI 是主动决策的业务大脑
```

**AI Agent 不是"帮你查票"，而是"帮你做决定"。**

---

## AI Agent 系统架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                        用户自然语言输入                                │
│   "我明天要去上海开会，下午3点前必须到，预算600以内，推荐一下"            │
└──────────────────────────────┬──────────────────────────────────────┘
                               ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     AgentRouter (意图识别 + 路由)                      │
│   LLM 分析用户意图 → 路由到专业 Agent → 支持混合意图串行/并行执行        │
└───────┬──────────┬──────────┬──────────┬──────────┬─────────────────┘
        ▼          ▼          ▼          ▼          ▼
  ┌──────────┐┌──────────┐┌──────────┐┌──────────┐┌──────────┐
  │ 智能购票  ││ 改签分析  ││ 候补预测  ││ 出行规划  ││ 智能客服  │
  │  Agent   ││  Agent   ││  Agent   ││  Agent   ││  Agent   │
  └────┬─────┘└────┬─────┘└────┬─────┘└────┬─────┘└────┬─────┘
       │           │           │           │           │
       ▼           ▼           ▼           ▼           ▼
  ┌──────────────────────────────────────────────────────────────┐
  │                   Function Calling 工具层                     │
  │                                                              │
  │  searchTrains()      getTicketPrice()    checkAvailability() │
  │  getPassengers()     createOrder()       getWaitlistStats()  │
  │  searchTransfer()    predictSuccessRate() getUserPreference()│
  │  getWeatherInfo()    getHolidayInfo()    getTrainDelay()     │
  └──────────────────────────────────────────────────────────────┘
                               │
       ┌───────────┬───────────┼───────────┬───────────┐
       ▼           ▼           ▼           ▼           ▼
  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
  │ MySQL  │ │ Redis  │ │ Kafka  │ │ 向量库  │ │ LLM    │
  │ 订单   │ │ 缓存   │ │ 事件   │ │ RAG    │ │ GPT-4o │
  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘
```

---

## 六大 AI Agent 详解

### 1. 智能购票 Agent (SmartBuyAgent)

**核心能力**：不是帮你查票，而是帮你做决策。

```
用户: "明天去上海，3点前到，预算600，靠窗座位"

Agent 思考链:
  1. 调用 searchTrains("北京", "上海", "明天") → 获取所有车次
  2. 调用 checkAvailability() → 过滤有票车次
  3. 调用 getTicketPrice() → 计算各车次票价
  4. 应用用户约束:
     - 3点前到 → 过滤 arrivalTime < 15:00
     - 预算600 → 过滤 price <= 600
     - 靠窗 → 优先推荐 A/F 座位
  5. 综合评分排序:
     - 时间匹配度 40% + 价格竞争力 30% + 座位偏好 20% + 历史偏好 10%
  6. 生成推荐理由:
     "推荐 G1 次列车，09:00发车13:28到达，二等座 ¥553，
      有 05车12A 靠窗座位可用。比 G3 次便宜 ¥47，早到28分钟。"

Agent 输出:
  - 推荐方案 (Top 3) + 推荐理由
  - 备选方案 (如果首选无票)
  - 风险提示 (节假日票源紧张建议尽早购买)
```

**Function Calling 工具**：
- `searchTrains(from, to, date)` → 查询可用列车
- `getTicketPrice(trainNo, from, to, seatType)` → 查询票价
- `checkAvailability(trainNo, date, seatType)` → 检查余票
- `getPassengers(userId)` → 获取乘车人列表
- `createOrder(trainNo, date, passengers)` → 创建订单

---

### 2. 改签分析 Agent (SmartChangeAgent)

**核心能力**：分析改签成本收益，给出最优方案。

```
用户: "G1 改签到明天，有没有更便宜的车次？"

Agent 分析:
  1. 调用 getOrderDetail("G1_order_no") → 获取当前订单
  2. 调用 searchTrains() → 获取可改签车次
  3. 计算改签成本:
     - 改签手续费: 免费 (开车前48h)
     - 票价差额: G3 便宜 ¥47 → 退差价
     - 座位风险: G3 余票紧张，可能无靠窗
  4. 生成改签建议:
     "建议改签到 G3 次，可退差价 ¥47。但 G3 余票仅剩 3 张，
      建议尽快操作。改签后座位可能变为 12F (靠走廊)。"

Agent 输出:
  - 改签方案对比 (当前 vs 候选)
  - 费用变化明细
  - 座位变化风险
  - 操作建议 (立即改签 / 等待 / 放弃)
```

**Function Calling 工具**：
- `getOrderDetail(orderNo)` → 获取当前订单
- `searchTrains(from, to, date)` → 查询可改签车次
- `getTicketPrice(trainNo, from, to, seatType)` → 查询票价
- `checkAvailability(trainNo, date, seatType)` → 检查余票

---

### 3. 候补分析 Agent (WaitlistAnalysisAgent)

**核心能力**：基于历史数据和当前状态，预测候补成功概率。

```
用户: "G1 候补了3天了，还有希望吗？"

Agent 分析:
  1. 调用 getWaitlistStats("G1", "2026-07-10") → 候补队列状态
  2. 调用 predictSuccessRate() → 基于历史数据预测
  3. 分析维度:
     - 当前候补位置: 第 23 位
     - 该车次历史退票率: 8.5%
     - 距离开车时间: 3天
     - 历史同期兑现率: 72%
     - 当前候补人数: 156 人
  4. 生成预测:
     "根据历史数据分析，您的候补成功概率约为 65%。
      该车次在发车前 24-48h 是退票高峰期，建议耐心等待。
      如果发车前 12h 仍未兑现，建议考虑备选方案 G3。"

Agent 输出:
  - 成功概率 (置信区间)
  - 关键时间节点 (退票高峰)
  - 备选方案
  - 操作建议 (继续等待 / 同时候补其他车次 / 放弃)
```

**Function Calling 工具**：
- `getWaitlistStats(trainNo, date)` → 候补队列统计
- `predictSuccessRate(trainNo, date, position)` → 成功率预测
- `searchTrains(from, to, date)` → 备选车次

---

### 4. 出行规划 Agent (RoutePlanAgent)

**核心能力**：综合多维度信息，生成完整出行方案。

```
用户: "下周三去大理出差，帮我规划一下"

Agent 分析:
  1. 调用 searchTrains("北京", "大理") → 无直达车次
  2. 调用 searchTransfer("北京", "大理") → 中转方案
  3. 调用 getWeatherInfo("大理", "下周三") → 天气预报
  4. 调用 getHolidayInfo("下周三") → 是否节假日
  5. 综合分析:
     - 中转方案: 北京→昆明(高铁) + 昆明→大理(动车)
     - 天气: 多云转小雨，建议带伞
     - 票价: 二等座 ¥1,250 (昆明中转)
     - 耗时: 约 12 小时
  6. 生成出行方案:
     "推荐方案: 北京南→昆明南 G405 (08:00-17:30) + 昆明→大理 D8672 (18:30-20:15)
      总耗时约 12 小时，二等座 ¥1,250。
      注意: 下周三大理有小雨，建议携带雨具。
      备选: 北京→昆明飞机(3h) + 昆明→大理动车(2h)，总费用约 ¥1,800。"

Agent 输出:
  - 最优方案 (时间/价格/舒适度)
  - 天气/节假日提醒
  - 备选方案
  - 出行清单 (证件/物品)
```

**Function Calling 工具**：
- `searchTrains(from, to, date)` → 直达车次
- `searchTransfer(from, to, date)` → 中转方案
- `getWeatherInfo(city, date)` → 天气信息
- `getHolidayInfo(date)` → 节假日信息

---

### 5. 智能客服 Agent (CustomerServiceAgent)

**核心能力**：RAG 检索 + 多轮对话，不是关键词匹配。

```
用户: "我买的票能退吗？扣多少钱？"

Agent 处理:
  1. RAG 检索: 向量检索退票政策文档
  2. 获取用户订单: 调用 getOrderDetail()
  3. 计算退票费:
     - 订单: G1, 2026-07-10, 二等座 ¥553
     - 当前时间: 2026-07-08 (距发车 48h)
     - 退票规则: 48h 以上免费
  4. 生成回答:
     "您购买的 G1 次列车(7月10日)可以退票。
      由于距发车还有 48 小时以上，退票手续费为 0 元。
      退款将在 1-3 个工作日内原路退回。
      需要我帮您办理退票吗？"

知识库内容 (RAG):
  - 退票规则: 48h免费 / 24-48h 5% / 2-24h 10% / 2h内 20%
  - 改签规则: 开车前可改签一次，票价多退少补
  - 候补规则: 候补成功需在 30 分钟内支付
  - 特殊人群: 儿童/学生/军人票优惠政策
  - 常见问题: 证件丢失/身份证过期/多人购票
```

**Function Calling 工具**：
- `searchKnowledge(query)` → RAG 知识库检索
- `getOrderDetail(orderNo)` → 获取用户订单
- `calculateRefundFee(orderNo)` → 计算退票费

---

### 6. 运维分析 Agent (OpsAgent) [可选]

**核心能力**：分析系统指标，解释异常原因。

```
运维人员: "今天下单接口 RT 突然升高，什么情况？"

Agent 分析:
  1. 调用 getMetrics("order-create", "1h") → 获取指标
  2. 分析维度:
     - P99 RT: 从 50ms 升至 800ms
     - QPS: 从 3000 升至 8000
     - Redis 命中率: 从 95% 降至 60%
     - Kafka 堆积: 12000 条
  3. 根因分析:
     "RT 升高原因分析:
      1. QPS 从 3000 升至 8000，可能是热门车次开售
      2. Redis 命中率下降导致大量请求穿透到 MySQL
      3. Kafka 堆积导致异步下单延迟
      建议操作:
      - 立即: 开启本地缓存预热热门车次
      - 短期: 增加 Kafka 消费者实例
      - 长期: 优化 Redis 缓存策略"

Agent 输出:
  - 异常指标
  - 根因分析
  - 影响范围
  - 修复建议
```

---

## 技术架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Vue3 前端                                    │
│   Element Plus + Pinia + SSE 流式对话                                │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────────┐
│                    Spring Cloud Gateway (8080)                       │
│              路由 / 限流 / JWT校验 / 请求日志                          │
└───────┬──────────┬──────────┬──────────┬──────────┬─────────────────┘
        │          │          │          │          │
   ┌────▼───┐ ┌───▼────┐ ┌───▼────┐ ┌───▼────┐ ┌───▼────┐
   │  User  │ │ Train  │ │ Ticket │ │ Order  │ │  AI    │
   │ (8081) │ │ (8082) │ │ (8083) │ │ (8084) │ │ (8087) │
   └───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘
       │          │          │          │          │
   ┌───▼──────────▼──────────▼──────────▼──────────▼────┐
   │                    基础设施层                         │
   │   MySQL 8.0  │  Redis 7  │  Kafka 3.7  │  向量库    │
   └─────────────────────────────────────────────────────┘
```

### 技术栈

| 类别 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 语言 | Java | 21 | 主力开发语言 |
| 框架 | Spring Boot | 3.3.x | 基础框架 |
| **AI 核心** | **Spring AI** | **1.0.0-M6** | **LLM 集成、Function Calling、RAG** |
| **AI 模型** | **OpenAI GPT-4o** | - | **对话、推理、意图识别** |
| **向量检索** | **Spring AI VectorStore** | - | **RAG 知识库检索** |
| ORM | MyBatis Plus | 3.5.7 | 数据持久化 |
| 缓存 | Redis | 7.x | 分布式缓存/锁 |
| 分布式锁 | Redisson | 3.x | 座位锁定 |
| 消息 | Apache Kafka | 3.x | 异步消息 |
| 数据库 | MySQL | 8.0 | 主数据库 |
| 网关 | Spring Cloud Gateway | 4.x | API 网关 |
| 限流 | Sentinel | 1.8.x | 流量控制 |
| 本地缓存 | Caffeine | 3.x | L1 缓存 |
| 前端 | Vue 3 + Vite | 3.x | 前端框架 |
| 容器 | Docker / Compose | - | 容器化部署 |

### Spring AI 核心能力

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: ${OPENAI_BASE_URL}
      chat:
        options:
          model: gpt-4o
          temperature: 0.7
```

| 能力 | Spring AI 实现 | 应用场景 |
|------|---------------|---------|
| 对话 | `ChatClient` | 智能客服、多轮对话 |
| Function Calling | `@Description` + `@Tool` | Agent 调用业务接口 |
| RAG | `VectorStore` + `DocumentRetriever` | 政策文档检索 |
| 流式输出 | `Flux<String>` (SSE) | 实时对话体验 |
| 向量化 | `EmbeddingModel` | 知识库向量化 |
| 记忆 | `ChatMemory` (Redis) | 多轮对话上下文 |

---

## 模块结构

```
railmind-ai/
├── railmind-common          公共模块 (工具/异常/常量/配置)
├── railmind-gateway         API 网关 (8080)
├── railmind-user            用户服务 (8081)
├── railmind-train           车次服务 (8082)
├── railmind-ticket          票务服务 (8083) - 核心高频
├── railmind-order           订单服务 (8084)
├── railmind-payment         支付服务 (8085)
├── railmind-message         消息服务 (8086) - WebSocket
├── railmind-ai              AI 服务 (8087) - Spring AI
└── railmind-admin           管理后台 (8088)
```

### DDD 分层架构

```
Controller  →  Service (接口)  →  ServiceImpl (实现)  →  Mapper  →  MySQL
                    ↓
            DomainService (领域逻辑)
                    ↓
              Event / Producer / Consumer (事件驱动)
```

---

## 核心业务流程

### 购票流程 (高并发场景)

```
用户请求 → Gateway 限流 → Redis Lua 原子扣库存 → Kafka 异步 → MySQL 落库
                │                    │                    │
                ▼                    ▼                    ▼
          令牌桶限流           防超卖第一道防线         削峰填谷
          100,000 QPS         原子性保证              异步解耦
```

### AI 决策流程

```
用户自然语言 → AgentRouter 意图识别 → 专业 Agent → Function Calling → 业务服务
                    │                      │              │
                    ▼                      ▼              ▼
              LLM 意图分类          Prompt + 上下文    工具调用 + 结果
              路由到最佳 Agent      多轮对话记忆       生成最终回答
```

---

## 高并发方案

### 分层防护

```
┌─────────────────────────────────────────┐
│  L1: CDN + 静态资源                      │
│  L2: Nginx 负载均衡                      │
│  L3: Gateway 全局限流 (令牌桶)            │
│  L4: 服务层限流 (Redis 计数器)            │
│  L5: 业务层防护 (Redis Lua 预扣)          │
│  L6: 数据层 (乐观锁 + 分布式锁)           │
└─────────────────────────────────────────┘
```

### 核心指标

| 场景 | 目标 QPS | 方案 |
|------|---------|------|
| 余票查询 | 50,000+ | Caffeine → Redis → MySQL 三级缓存 |
| 下单 | 5,000+ | Redis Lua 原子扣减 + Kafka 异步 |
| 座位锁定 | 3,000+ | Redisson 分布式锁 + 15min TTL |

---

## 快速启动

### 环境要求

- JDK 21
- Maven 3.9+
- Docker + Docker Compose
- Node.js 18+ (前端)

### 一键启动基础设施

```bash
cd docker
docker-compose up -d
```

启动 MySQL 8.0 + Redis 7 + Kafka 3.7 + Zookeeper

### 初始化数据库

```bash
mysql -u root -p123456 < sql/V1__init_schema.sql
mysql -u root -p123456 railmind < sql/V2__station_data.sql
mysql -u root -p123456 railmind < sql/V3__train_data.sql
mysql -u root -p123456 railmind < sql/V4__init_user.sql
mysql -u root -p123456 railmind < sql/V5__seat_type_data.sql
```

### 启动服务

```bash
# 编译所有模块
mvn clean compile -DskipTests

# 启动各服务
mvn spring-boot:run -pl railmind-user
mvn spring-boot:run -pl railmind-train
mvn spring-boot:run -pl railmind-ticket
mvn spring-boot:run -pl railmind-order
mvn spring-boot:run -pl railmind-ai
```

### 启动前端

```bash
cd vue-frontend
npm install
npm run dev
```

---

## API 文档

各服务启动后访问 Swagger UI:

| 服务 | 地址 |
|------|------|
| 用户服务 | http://localhost:8081/swagger-ui.html |
| 车次服务 | http://localhost:8082/swagger-ui.html |
| 票务服务 | http://localhost:8083/swagger-ui.html |
| 订单服务 | http://localhost:8084/swagger-ui.html |
| AI 服务 | http://localhost:8087/swagger-ui.html |

---

## 接口示例

### 创建订单

```bash
curl -X POST http://localhost:8084/api/order/create \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "trainId": 1,
    "trainNo": "G1",
    "travelDate": "2026-07-10",
    "fromStation": "BJP",
    "fromStationName": "北京南",
    "toStation": "SHH",
    "toStationName": "上海虹桥",
    "departureTime": "09:00",
    "arrivalTime": "13:28",
    "passengers": [{
      "passengerId": 1,
      "passengerName": "张三",
      "idCard": "encrypted",
      "seatTypeCode": "ZE",
      "seatTypeName": "二等座",
      "ticketPrice": 553.00
    }]
  }'
```

### AI 对话 (SSE 流式)

```bash
curl -N http://localhost:8087/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "session_001",
    "message": "明天去上海，3点前到，预算600，推荐一下"
  }'
```

---

## License

MIT
