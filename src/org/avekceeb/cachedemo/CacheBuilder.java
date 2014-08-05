package org.avekceeb.cachedemo;

public class CacheBuilder<K,V> {
	public Cacheable<K,V> createCache(String strategy, int size, long ttl) {
        switch (strategy) {
            case "fifo":
            case "FIFO":
                return new Cache<K,V>(size, ttl, new FIFOTable<K,CacheEntry<V> >(size));
            case "lru":
            case "LRU":
                return new Cache<K,V>(size, ttl, new LRUTable<K,CacheEntry<V> >(size));
            default: return null;
        }
	}
}