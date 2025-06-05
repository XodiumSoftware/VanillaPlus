/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.mapify.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class Cache<K, V> {

    private final Map<K, CacheItem> map = new HashMap<>();
    private final Function<K, V> getter;
    private long cacheDuration;

    public Cache(long cacheDurationMs, Function<K, V> getter) {
        this.cacheDuration = cacheDurationMs;
        this.getter = getter;
    }

    public V get(K key) {
        if (map.containsKey(key)) {
            CacheItem item = map.get(key);
            if (!item.isExpired()) {
                item.refresh();
                return item.value;
            }
        }
        V newValue = this.getter.apply(key);
        this.map.put(key, new CacheItem(newValue));
        return newValue;
    }

    /**
     * Clears the expired items in the cache
     *
     * @return the amount of cleared values
     */
    public int clearExpired() {
        AtomicInteger count = new AtomicInteger(0);
        map.entrySet().removeIf(value -> {
            if (value.getValue().isExpired()) {
                count.incrementAndGet();
                return true;
            }
            return false;
        });
        return count.get();
    }

    public long getCacheDuration() {
        return cacheDuration;
    }

    public void setCacheDuration(long cacheDuration) {
        this.cacheDuration = cacheDuration;
    }

    private class CacheItem {

        private final V value;
        private long insertedTime;

        public CacheItem(V value) {
            this.insertedTime = System.currentTimeMillis();
            this.value = value;
        }

        public void refresh() {
            this.insertedTime = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - this.insertedTime > cacheDuration;
        }
    }
}
