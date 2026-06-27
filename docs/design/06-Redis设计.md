# RailMind AI - Redis 设计

> 版本：v1.0 | 日期：2026-06-27

---

## 一、Key 设计规范

```
{业务}:{模块}:{维度}:{标识}
```

---

## 二、Key 清单

### 2.1 用户域

| Key | 类型 | TTL | 说明 |
|-----|------|-----|------|
| `user:token:{accessToken}` | String | 15min | Access Token → userId |
| `user:refresh:{refreshToken}` | String | 7d | Refresh Token → userId |
| `user:info:{userId}` | Hash | 30min | 用户基本信息缓存 |
| `user:login:fail:{phone}` | String | 30min | 登录失败次数（防爆破） |
| `user:captcha:{phone}` | String | 5min | 短信验证码 |

### 2.2 车次域

| Key | 类型 | TTL | 说明 |
|-----|------|-----|------|
| `train:info:{trainId}` | Hash | 1h | 车次基本信息 |
| `train:stations:{trainId}` | List | 1h | 途经站列表 |
| `station:search:{keyword}` | String | 30min | 站点搜索结果缓存 |

### 2.3 票务域（核心）

| Key | 类型 | TTL | 说明 |
|-----|------|-----|------|
| `ticket:stock:{trainId}:{date}:{from}:{to}:{seatType}` | String | 30s | 库存数量 |
| `ticket:query:{trainId}:{date}` | Hash | 30s | 整车次余票快照 |
| `seat:lock:{trainId}:{date}:{seatType}` | Set | 15min | 已锁定座位集合 |
| `seat:lock:detail:{trainId}:{date}:{seatNo}` | String | 15min | 座位锁详情 → orderNo |

### 2.4 订单域

| Key | 类型 | TTL | 说明 |
|-----|------|-----|------|
| `order:info:{orderNo}` | Hash | 30min | 订单详情缓存 |
| `order:timeout:{orderNo}` | String | 15min | 超时检测（TTL驱动取消） |
| `order:user:list:{userId}` | ZSet | 1h | 用户订单列表（score=时间戳） |

### 2.5 候补队列

| Key | 类型 | TTL | 说明 |
|-----|------|-----|------|
| `waitlist:queue:{trainId}:{date}:{from}:{to}:{seatType}` | ZSet | 24h | 候补队列（score=priority） |

### 2.6 限流

| Key | 类型 | TTL | 说明 |
|-----|------|-----|------|
| `rate:global:{接口}:{window}` | String | 动态 | 全局限流计数 |
| `rate:user:{userId}:{接口}:{window}` | String | 动态 | 用户级限流 |

### 2.7 WebSocket

| Key | 类型 | TTL | 说明 |
|-----|------|-----|------|
| `ws:user:{userId}` | String | 2h | 用户WebSocket连接信息 |
| `ws:subscribe:{topic}` | Set | 2h | 主题订阅者列表 |

---

## 三、缓存架构图

```
                    ┌─────────────────────────────────────┐
                    │              Redis Cluster            │
                    │                                       │
                    │  ┌──────────┐  ┌──────────┐          │
                    │  │  Master  │  │  Master  │  ...     │
                    │  │  Node 1  │  │  Node 2  │          │
                    │  └────┬─────┘  └────┬─────┘          │
                    │       │             │                │
                    │  ┌────▼─────┐  ┌────▼─────┐          │
                    │  │  Slave   │  │  Slave   │          │
                    │  │  Node 1  │  │  Node 2  │          │
                    │  └──────────┘  └──────────┘          │
                    └─────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
              ┌──────────┐   ┌──────────┐   ┌──────────┐
              │ 热点数据  │   │ 会话数据  │   │ 分布式锁 │
              │ 余票缓存  │   │ Token    │   │ 座位锁   │
              │ TTL:30s  │   │ TTL:15m  │   │ TTL:15m  │
              └──────────┘   └──────────┘   └──────────┘
```

---

## 四、缓存一致性策略

### 4.1 余票缓存：Cache Aside + 主动过期

- 读：先查 Redis，miss 则查 DB 并回写
- 写：先扣 DB 库存，再删 Redis 缓存（异步刷新）
- TTL 极短（30s），即使不一致也只影响极短窗口

### 4.2 用户信息：Cache Aside + TTL 兜底

- 用户修改信息时主动删除缓存
- TTL 30min 兜底

### 4.3 分布式锁：Redisson

- 座位锁定使用 Redisson 分布式锁
- 看门狗自动续期
- 锁粒度：`trainId:date:seatNo`

---

## 五、缓存穿透/击穿/雪崩防护

| 问题 | 防护方案 |
|------|---------|
| 穿透 | 布隆过滤器预判 + 空值缓存（TTL 5min） |
| 击穿 | 热点Key永不过期 + 互斥锁重建 |
| 雪崩 | TTL随机偏移（±5s）+ 多级缓存 |
