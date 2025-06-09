package com.example.mylab.counter;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RequestCounter {
    private final ConcurrentMap<String, AtomicInteger> methodCallCounts = new ConcurrentHashMap<>();

    public synchronized void increment(String methodName) {
        methodCallCounts.computeIfAbsent(methodName, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public synchronized int getCount(String methodName) {
        return methodCallCounts.getOrDefault(methodName, new AtomicInteger(0)).get();
    }

    public synchronized void reset(String methodName) {
        methodCallCounts.put(methodName, new AtomicInteger(0));
    }

    public synchronized ConcurrentMap<String, Integer> getAllCounts() {
        ConcurrentMap<String, Integer> result = new ConcurrentHashMap<>();
        methodCallCounts.forEach((key, value) -> result.put(key, value.get()));
        return result;
    }
}