package org.avekceeb.cachedemo;

public interface CacheTable<K,V>  extends Iterable<K> {
    public V get(K k);
    public void put(K k, V v);
    public void del(K k);
    public void clear();
}
