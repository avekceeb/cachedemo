package org.avekceeb.cachedemo;

public class CacheEntry<V> {
    public V value;
    public long ttl;
    @SuppressWarnings("unused")
    private CacheEntry() {}
    public CacheEntry(V v, long t) {
        value = v;
        ttl = t;
    }
}
