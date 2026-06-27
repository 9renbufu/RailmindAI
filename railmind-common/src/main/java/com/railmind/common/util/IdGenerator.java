package com.railmind.common.util;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 雪花算法ID生成器
 * 结构: 1位符号 + 41位时间戳 + 5位数据中心 + 5位机器 + 12位序列号
 */
public class IdGenerator {

    private static final long EPOCH = 1704067200000L; // 2024-01-01 00:00:00
    private static final long DATA_CENTER_ID_BITS = 5L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    private final long dataCenterId;
    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private static final IdGenerator INSTANCE = new IdGenerator(1, 1);

    public IdGenerator(long dataCenterId, long workerId) {
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("dataCenterId out of range");
        }
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("workerId out of range");
        }
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
    }

    public synchronized long nextId() {
        long timestamp = currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID");
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    public static long generateId() {
        return INSTANCE.nextId();
    }

    public static String generateOrderNo() {
        return String.valueOf(generateId());
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
