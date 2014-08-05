package org.avekceeb.cachedemo;

public interface Cacheable<K,V> {
    public static final long Second = 1000;
    public static final long Minute = 60 * Second;
    public V get(K key);
    public void put(K key, V value);
    public void remove(K key);
    public void clear();
    public int size();
}
