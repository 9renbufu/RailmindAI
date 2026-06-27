package com.railmind.common;

import com.railmind.common.util.IdGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IdGeneratorTest {

    @Test
    void generateId_shouldReturnPositive() {
        long id = IdGenerator.generateId();
        assertTrue(id > 0);
    }

    @Test
    void generateId_shouldBeUnique() {
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(IdGenerator.generateId());
        }
        assertEquals(1000, ids.size());
    }

    @Test
    void generateOrderNo_shouldReturnNonEmpty() {
        String orderNo = IdGenerator.generateOrderNo();
        assertNotNull(orderNo);
        assertFalse(orderNo.isEmpty());
    }

    @Test
    void generateId_shouldBeIncreasing() {
        long id1 = IdGenerator.generateId();
        long id2 = IdGenerator.generateId();
        assertTrue(id2 > id1);
    }
}
