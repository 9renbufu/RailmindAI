-- ============================================================
-- RailMind AI - 智能铁路售票系统 数据库初始化脚本
-- 版本: V1__init_schema.sql
-- 数据库: MySQL 8.0
-- 字符集: utf8mb4
-- ============================================================

-- 创建数据库（如不存在）
CREATE DATABASE IF NOT EXISTS railmind
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE railmind;

-- ============================================================
-- 1. 用户表 t_user
-- ============================================================
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username        VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password        VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    real_name       VARCHAR(50)  COMMENT '真实姓名',
    id_card         VARCHAR(255) COMMENT '身份证号(AES加密存储)',
    id_card_hash    VARCHAR(64)  COMMENT '身份证号SHA256哈希(用于唯一性校验)',
    phone           VARCHAR(20)  NOT NULL UNIQUE COMMENT '手机号',
    email           VARCHAR(100) COMMENT '邮箱',
    avatar          VARCHAR(500) COMMENT '头像URL',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    user_level      TINYINT      NOT NULL DEFAULT 1 COMMENT '用户等级: 1-普通 2-银卡 3-金卡 4-钻石',
    last_login_time DATETIME     COMMENT '最后登录时间',
    last_login_ip   VARCHAR(45)  COMMENT '最后登录IP',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-正常 1-已删除',
    INDEX idx_phone (phone),
    INDEX idx_id_card_hash (id_card_hash),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. 乘车人表 t_passenger
-- ============================================================
DROP TABLE IF EXISTS t_passenger;
CREATE TABLE t_passenger (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '乘车人ID',
    user_id         BIGINT       NOT NULL COMMENT '所属用户ID',
    name            VARCHAR(50)  NOT NULL COMMENT '姓名',
    id_card         VARCHAR(255) NOT NULL COMMENT '身份证号(AES加密)',
    id_card_hash    VARCHAR(64)  NOT NULL COMMENT '身份证号哈希',
    phone           VARCHAR(20)  COMMENT '手机号',
    type            TINYINT      NOT NULL DEFAULT 1 COMMENT '旅客类型: 1-成人 2-儿童 3-学生 4-军人',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_id_card_hash (id_card_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='乘车人表';

-- ============================================================
-- 3. 站点表 t_station
-- ============================================================
DROP TABLE IF EXISTS t_station;
CREATE TABLE t_station (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '站点ID',
    code            VARCHAR(10)  NOT NULL UNIQUE COMMENT '站点编码(BJP=北京)',
    name            VARCHAR(50)  NOT NULL COMMENT '站名',
    city            VARCHAR(50)  NOT NULL COMMENT '所属城市',
    province        VARCHAR(50)  NOT NULL COMMENT '省份',
    longitude       DECIMAL(10,6) COMMENT '经度',
    latitude        DECIMAL(10,6) COMMENT '纬度',
    bureau          VARCHAR(50)  COMMENT '所属路局',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-正常',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_city (city),
    INDEX idx_name (name),
    INDEX idx_province (province)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站点表';

-- ============================================================
-- 4. 车次表 t_train
-- ============================================================
DROP TABLE IF EXISTS t_train;
CREATE TABLE t_train (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '车次ID',
    train_no        VARCHAR(20)  NOT NULL UNIQUE COMMENT '车次号(G1234)',
    train_type      CHAR(1)      NOT NULL COMMENT '车次类型: G-高铁 D-动车 C-城际 Z-直达 T-特快 K-快速 L-临客',
    start_station   VARCHAR(10)  NOT NULL COMMENT '始发站编码',
    end_station     VARCHAR(10)  NOT NULL COMMENT '终到站编码',
    departure_time  TIME         NOT NULL COMMENT '发车时间',
    arrival_time    TIME         NOT NULL COMMENT '到达时间',
    run_days        TINYINT      NOT NULL DEFAULT 1 COMMENT '运行天数(跨天运行>1)',
    total_mileage   INT          NOT NULL COMMENT '总里程(km)',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-停运 1-正常',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_train_no (train_no),
    INDEX idx_start_end (start_station, end_station),
    INDEX idx_train_type (train_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车次表';

-- ============================================================
-- 5. 车次途经站表 t_train_station
-- ============================================================
DROP TABLE IF EXISTS t_train_station;
CREATE TABLE t_train_station (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    train_id        BIGINT       NOT NULL COMMENT '车次ID',
    station_code    VARCHAR(10)  NOT NULL COMMENT '站点编码',
    station_order   INT          NOT NULL COMMENT '站序(1=始发站)',
    arrival_time    TIME         COMMENT '到达时间(始发站为NULL)',
    departure_time  TIME         COMMENT '出发时间(终到站为NULL)',
    stop_duration   INT          DEFAULT 0 COMMENT '停靠时长(分钟)',
    mileage         INT          NOT NULL COMMENT '从始发站起的里程(km)',
    INDEX idx_train_id (train_id),
    INDEX idx_station_code (station_code),
    UNIQUE KEY uk_train_order (train_id, station_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车次途经站表';

-- ============================================================
-- 6. 座位类型表 t_seat_type
-- ============================================================
DROP TABLE IF EXISTS t_seat_type;
CREATE TABLE t_seat_type (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    train_id        BIGINT       NOT NULL COMMENT '车次ID',
    seat_type_code  VARCHAR(10)  NOT NULL COMMENT '座位编码(SW/YW/ZE等)',
    seat_type_name  VARCHAR(20)  NOT NULL COMMENT '座位名称(商务座/一等座/二等座等)',
    total_count     INT          NOT NULL COMMENT '总座位数',
    price_factor    DECIMAL(5,2) NOT NULL COMMENT '票价系数(相对二等座)',
    INDEX idx_train_id (train_id),
    UNIQUE KEY uk_train_seat (train_id, seat_type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位类型表';

-- ============================================================
-- 7. 运行图表 t_train_schedule (每日排列车次)
-- ============================================================
DROP TABLE IF EXISTS t_train_schedule;
CREATE TABLE t_train_schedule (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    train_id        BIGINT       NOT NULL COMMENT '车次ID',
    travel_date     DATE         NOT NULL COMMENT '运行日期',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-停运 1-正常 2-加开',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_train_date (train_id, travel_date),
    INDEX idx_travel_date (travel_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车次运行图';

-- ============================================================
-- 8. 票价表 t_ticket_price
-- ============================================================
DROP TABLE IF EXISTS t_ticket_price;
CREATE TABLE t_ticket_price (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    train_id        BIGINT       NOT NULL COMMENT '车次ID',
    from_station    VARCHAR(10)  NOT NULL COMMENT '出发站编码',
    to_station      VARCHAR(10)  NOT NULL COMMENT '到达站编码',
    seat_type_code  VARCHAR(10)  NOT NULL COMMENT '座位类型',
    price           DECIMAL(10,2) NOT NULL COMMENT '票价(元)',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_train_from_to_seat (train_id, from_station, to_station, seat_type_code),
    INDEX idx_from_to (from_station, to_station)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='票价表';

-- ============================================================
-- 9. 票务库存表 t_ticket_inventory (核心高频表)
-- ============================================================
DROP TABLE IF EXISTS t_ticket_inventory;
CREATE TABLE t_ticket_inventory (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    train_id        BIGINT       NOT NULL COMMENT '车次ID',
    travel_date     DATE         NOT NULL COMMENT '乘车日期',
    from_station    VARCHAR(10)  NOT NULL COMMENT '出发站编码',
    to_station      VARCHAR(10)  NOT NULL COMMENT '到达站编码',
    seat_type_code  VARCHAR(10)  NOT NULL COMMENT '座位类型',
    total_count     INT          NOT NULL COMMENT '总票数',
    sold_count      INT          NOT NULL DEFAULT 0 COMMENT '已售数量',
    locked_count    INT          NOT NULL DEFAULT 0 COMMENT '锁定中数量',
    version         BIGINT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_train_date_from_to_seat (train_id, travel_date, from_station, to_station, seat_type_code),
    INDEX idx_travel_date (travel_date),
    INDEX idx_train_date (train_id, travel_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='票务库存表';

-- ============================================================
-- 10. 座位锁定表 t_seat_lock
-- ============================================================
DROP TABLE IF EXISTS t_seat_lock;
CREATE TABLE t_seat_lock (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    train_id        BIGINT       NOT NULL COMMENT '车次ID',
    travel_date     DATE         NOT NULL COMMENT '乘车日期',
    seat_type_code  VARCHAR(10)  NOT NULL COMMENT '座位类型',
    seat_no         VARCHAR(10)  NOT NULL COMMENT '座位号(如 05车12A)',
    order_no        VARCHAR(32)  COMMENT '关联订单号',
    user_id         BIGINT       NOT NULL COMMENT '锁定用户ID',
    lock_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '锁定时间',
    expire_time     DATETIME     NOT NULL COMMENT '锁过期时间',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-已释放 1-锁定中',
    INDEX idx_train_date_seat (train_id, travel_date, seat_type_code, seat_no),
    INDEX idx_expire (expire_time, status),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位锁定表';

-- ============================================================
-- 11. 订单表 t_order (核心)
-- ============================================================
DROP TABLE IF EXISTS t_order;
CREATE TABLE t_order (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no        VARCHAR(32)  NOT NULL UNIQUE COMMENT '订单号(雪花算法)',
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    train_id        BIGINT       NOT NULL COMMENT '车次ID',
    train_no        VARCHAR(20)  NOT NULL COMMENT '车次号',
    travel_date     DATE         NOT NULL COMMENT '乘车日期',
    from_station    VARCHAR(10)  NOT NULL COMMENT '出发站编码',
    from_station_name VARCHAR(50) NOT NULL COMMENT '出发站名',
    to_station      VARCHAR(10)  NOT NULL COMMENT '到达站编码',
    to_station_name VARCHAR(50)  NOT NULL COMMENT '到达站名',
    departure_time  TIME         NOT NULL COMMENT '发车时间',
    arrival_time    TIME         NOT NULL COMMENT '到达时间',
    total_amount    DECIMAL(10,2) NOT NULL COMMENT '订单总金额(元)',
    status          VARCHAR(20)  NOT NULL DEFAULT 'CREATED' COMMENT '订单状态',
    pay_deadline    DATETIME     NOT NULL COMMENT '支付截止时间',
    paid_at         DATETIME     COMMENT '支付完成时间',
    cancelled_at    DATETIME     COMMENT '取消时间',
    cancel_reason   VARCHAR(255) COMMENT '取消原因',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_travel_date (travel_date),
    INDEX idx_pay_deadline (pay_deadline),
    INDEX idx_train_date (train_id, travel_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================================
-- 12. 订单明细表 t_order_item
-- ============================================================
DROP TABLE IF EXISTS t_order_item;
CREATE TABLE t_order_item (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    order_id        BIGINT       NOT NULL COMMENT '订单ID',
    order_no        VARCHAR(32)  NOT NULL COMMENT '订单号',
    passenger_id    BIGINT       NOT NULL COMMENT '乘车人ID',
    passenger_name  VARCHAR(50)  NOT NULL COMMENT '乘车人姓名',
    id_card         VARCHAR(255) NOT NULL COMMENT '身份证号(加密)',
    id_card_hash    VARCHAR(64)  NOT NULL COMMENT '身份证号哈希',
    seat_type_code  VARCHAR(10)  NOT NULL COMMENT '座位类型',
    seat_type_name  VARCHAR(20)  NOT NULL COMMENT '座位名称',
    seat_no         VARCHAR(10)  COMMENT '座位号(如 05车12A)',
    ticket_price    DECIMAL(10,2) NOT NULL COMMENT '票价(元)',
    status          VARCHAR(20)  NOT NULL DEFAULT 'NORMAL' COMMENT '状态: NORMAL/REFUNDED/CHANGED',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- ============================================================
-- 13. 支付表 t_payment
-- ============================================================
DROP TABLE IF EXISTS t_payment;
CREATE TABLE t_payment (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付ID',
    payment_no      VARCHAR(32)  NOT NULL UNIQUE COMMENT '支付单号',
    order_no        VARCHAR(32)  NOT NULL COMMENT '关联订单号',
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    amount          DECIMAL(10,2) NOT NULL COMMENT '支付金额(元)',
    pay_type        VARCHAR(20)  NOT NULL DEFAULT 'MOCK' COMMENT '支付方式: WECHAT/ALIPAY/MOCK',
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/SUCCESS/FAILED/REFUNDED',
    paid_at         DATETIME     COMMENT '支付成功时间',
    refund_no       VARCHAR(32)  COMMENT '退款单号',
    refund_amount   DECIMAL(10,2) COMMENT '退款金额',
    refunded_at     DATETIME     COMMENT '退款时间',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_payment_no (payment_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付表';

-- ============================================================
-- 14. 候补购票表 t_waitlist
-- ============================================================
DROP TABLE IF EXISTS t_waitlist;
CREATE TABLE t_waitlist (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '候补ID',
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    train_id        BIGINT       NOT NULL COMMENT '车次ID',
    travel_date     DATE         NOT NULL COMMENT '乘车日期',
    from_station    VARCHAR(10)  NOT NULL COMMENT '出发站编码',
    to_station      VARCHAR(10)  NOT NULL COMMENT '到达站编码',
    seat_type_code  VARCHAR(10)  NOT NULL COMMENT '座位类型',
    passenger_ids   VARCHAR(255) NOT NULL COMMENT '乘车人ID列表(JSON数组)',
    priority        INT          NOT NULL DEFAULT 0 COMMENT '优先级(数值越大越优先)',
    status          VARCHAR(20)  NOT NULL DEFAULT 'WAITING' COMMENT '状态: WAITING/FULFILLED/CANCELLED/EXPIRED',
    expire_at       DATETIME     NOT NULL COMMENT '候补过期时间',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_train_date (train_id, travel_date),
    INDEX idx_user_id (user_id),
    INDEX idx_status_expire (status, expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='候补购票表';

-- ============================================================
-- 15. 退票记录表 t_refund
-- ============================================================
DROP TABLE IF EXISTS t_refund;
CREATE TABLE t_refund (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '退票ID',
    refund_no       VARCHAR(32)  NOT NULL UNIQUE COMMENT '退款单号',
    order_no        VARCHAR(32)  NOT NULL COMMENT '原订单号',
    order_item_id   BIGINT       NOT NULL COMMENT '订单明细ID',
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    ticket_price    DECIMAL(10,2) NOT NULL COMMENT '原票价',
    refund_fee      DECIMAL(10,2) NOT NULL COMMENT '退票手续费',
    refund_amount   DECIMAL(10,2) NOT NULL COMMENT '实际退款金额',
    reason          VARCHAR(255) COMMENT '退票原因',
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/SUCCESS/FAILED',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退票记录表';

-- ============================================================
-- 16. 改签记录表 t_change_ticket
-- ============================================================
DROP TABLE IF EXISTS t_change_ticket;
CREATE TABLE t_change_ticket (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '改签ID',
    change_no       VARCHAR(32)  NOT NULL UNIQUE COMMENT '改签单号',
    order_no        VARCHAR(32)  NOT NULL COMMENT '原订单号',
    order_item_id   BIGINT       NOT NULL COMMENT '订单明细ID',
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    old_train_no    VARCHAR(20)  NOT NULL COMMENT '原车次号',
    old_travel_date DATE         NOT NULL COMMENT '原乘车日期',
    old_seat_no     VARCHAR(10)  COMMENT '原座位号',
    new_train_no    VARCHAR(20)  NOT NULL COMMENT '新车次号',
    new_travel_date DATE         NOT NULL COMMENT '新乘车日期',
    new_seat_no     VARCHAR(10)  COMMENT '新座位号',
    price_diff      DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '票价差额(正=补差价, 负=退差价)',
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/SUCCESS/FAILED',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='改签记录表';

-- ============================================================
-- 17. 系统公告表 t_announcement
-- ============================================================
DROP TABLE IF EXISTS t_announcement;
CREATE TABLE t_announcement (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '公告ID',
    title           VARCHAR(200) NOT NULL COMMENT '公告标题',
    content         TEXT         NOT NULL COMMENT '公告内容',
    type            TINYINT      NOT NULL DEFAULT 1 COMMENT '类型: 1-系统公告 2-列车晚点 3-停运通知',
    priority        TINYINT      NOT NULL DEFAULT 0 COMMENT '优先级: 0-普通 1-重要 2-紧急',
    publish_time    DATETIME     COMMENT '发布时间',
    expire_time     DATETIME     COMMENT '过期时间',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿 1-已发布 2-已撤回',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- ============================================================
-- 18. 操作日志表 t_audit_log
-- ============================================================
DROP TABLE IF EXISTS t_audit_log;
CREATE TABLE t_audit_log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id         BIGINT       COMMENT '操作用户ID',
    username        VARCHAR(50)  COMMENT '用户名',
    action          VARCHAR(50)  NOT NULL COMMENT '操作类型',
    module          VARCHAR(50)  NOT NULL COMMENT '模块名称',
    target_type     VARCHAR(50)  COMMENT '操作对象类型',
    target_id       VARCHAR(50)  COMMENT '操作对象ID',
    detail          TEXT         COMMENT '操作详情',
    ip              VARCHAR(45)  COMMENT '请求IP',
    user_agent      VARCHAR(500) COMMENT 'User-Agent',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_module (module),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表';

-- ============================================================
-- 完成
-- ============================================================
SELECT 'RailMind AI 数据库初始化完成，共 18 张表' AS result;
