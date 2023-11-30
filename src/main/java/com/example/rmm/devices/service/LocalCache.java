package com.example.rmm.devices.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
@Service
public class LocalCache {

    private static final Map<Long, Double> CACHE = new ConcurrentHashMap<>();

    @Async
    public void put(final Long key, final Supplier<Double> value) {
        log.debug("Updating cache - thread: " + Thread.currentThread().getName());
        CACHE.put(key, value.get());
    }

    @Async
    public void remove(final Long key) {
        log.debug("Removing item from cache - thread: " + Thread.currentThread().getName());
        CACHE.remove(key);
    }

    public Double get(final Long key, final Supplier<Double> value) {
        return CACHE.computeIfAbsent(key, k -> value.get());
    }

    public void clear() {
        CACHE.clear();
    }
}
