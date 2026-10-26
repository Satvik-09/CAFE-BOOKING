package com.cafe.booking;

import com.cafe.booking.repository.MenuItemRepository;
import com.cafe.booking.tx.TxDemoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransactionRulesTest {

    @Autowired TxDemoService demo;
    @Autowired MenuItemRepository repo;

    private final List<String> created = new ArrayList<>();

    private String unique(String prefix) {
        String name = prefix + "-" + UUID.randomUUID();
        created.add(name);
        return name;
    }

    private boolean exists(String name) { return repo.existsByName(name); }

    @AfterEach
    void cleanUp() {
        repo.findAll().stream()
                .filter(m -> created.contains(m.getName()))
                .forEach(repo::delete);
        created.clear();
    }

    @Test
    void runtimeExceptionRollsBack() {
        String n = unique("rt");
        assertThrows(IllegalStateException.class, () -> demo.saveThenRuntimeFail(n));
        assertFalse(exists(n));
    }

    @Test
    void checkedExceptionDoesNotRollBackByDefault() {
        String n = unique("checked");
        assertThrows(Exception.class, () -> demo.saveThenCheckedFail(n));
        assertTrue(exists(n));
    }

    @Test
    void checkedExceptionRollsBackWithRollbackFor() {
        String n = unique("checked-rf");
        assertThrows(Exception.class, () -> demo.saveThenCheckedFailWithRollbackFor(n));
        assertFalse(exists(n));
    }

    @Test
    void selfInvocationBypassesTheTransactionProxy() {
        String n = unique("self");
        assertThrows(IllegalStateException.class, () -> demo.selfInvocation(n));
        assertTrue(exists(n));
    }

    @Test
    void requiresNewSurvivesOuterRollback() {
        String outer = unique("outer-new");
        String inner = unique("inner-new");
        assertThrows(IllegalStateException.class, () -> demo.outerFailsAfterInner(outer, inner, true));
        assertFalse(exists(outer));
        assertTrue(exists(inner));
    }

    @Test
    void requiredJoinsAndRollsBackWithOuter() {
        String outer = unique("outer-req");
        String inner = unique("inner-req");
        assertThrows(IllegalStateException.class, () -> demo.outerFailsAfterInner(outer, inner, false));
        assertFalse(exists(outer));
        assertFalse(exists(inner));
    }
}