/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.mapify.util

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Function

class Cache<K, V>(var cacheDuration: Long, private val getter: Function<K?, V?>) {
    private val map: MutableMap<K?, CacheItem> = HashMap<K?, CacheItem>()

    fun get(key: K?): V? {
        if (map.containsKey(key)) {
            val item: CacheItem = map[key]!!
            if (!item.isExpired) {
                item.refresh()
                return item.value
            }
        }
        val newValue = this.getter.apply(key)
        this.map.put(key, Cache.CacheItem(newValue))
        return newValue
    }

    /**
     * Clears the expired items in the cache
     *
     * @return the amount of cleared values
     */
    fun clearExpired(): Int {
        val count = AtomicInteger(0)
        map.entries.removeIf { value: MutableMap.MutableEntry<K?, CacheItem?>? ->
            if (value!!.value.isExpired()) {
                count.incrementAndGet()
                return@removeIf true.toInt()
            }
            false
        }
        return count.get()
    }

    inner class CacheItem(val value: V?) {
        private var insertedTime: Long

        init {
            this.insertedTime = System.currentTimeMillis()
        }

        fun refresh() {
            this.insertedTime = System.currentTimeMillis()
        }

        val isExpired: Boolean
            get() = System.currentTimeMillis() - this.insertedTime > cacheDuration
    }
}
