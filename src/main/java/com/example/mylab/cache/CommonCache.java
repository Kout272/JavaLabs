package com.example.mylab.cache;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class CommonCache {
    private static final Map<String, Object> cache = new HashMap<>();
    private static final Map<String, Map<Integer, Object>> idCache = new HashMap<>();

    public static void put(String key, Object value) {
        cache.put(key, value);
    }

    public static <T> T get(String key, Class<T> type) {
        Object value = cache.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public static void putWithId(String cacheName, Integer id, Object value) {
        idCache.computeIfAbsent(cacheName, k -> new HashMap<>()).put(id, value);
    }

    public static <T> T getById(String cacheName, Integer id, Class<T> type) {
        Map<Integer, Object> specificCache = idCache.get(cacheName);
        if (specificCache == null) return null;

        Object value = specificCache.get(id);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public static void removeById(String cacheName, Integer id) {
        Map<Integer, Object> specificCache = idCache.get(cacheName);
        if (specificCache != null) {
            specificCache.remove(id);
        }
    }

    public static void clearAll() {
        cache.clear();
        idCache.clear();
    }
}