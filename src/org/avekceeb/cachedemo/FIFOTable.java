package org.avekceeb.cachedemo;

import java.util.ArrayList;
import java.util.Iterator;

public class FIFOTable<K,V> implements CacheTable<K,V> {

    private ArrayList<K> keys;
    private ArrayList<V> values;
    private int tail;

    public FIFOTable(int size) {
        keys = new ArrayList<K>(size);
        values = new ArrayList<V>(size);
        for (int i = 0; i < size; i++) {
            keys.add(i, null);
            values.add(i, null);
        }
        tail = 0;
    }

    public V get(K key) {
        int i = keys.indexOf(key);
        if (-1 == i)
            return null;
        return values.get(i);
    }

    public void put(K key, V value) {
        int i = keys.indexOf(key);
        i = (-1 == i) ? tail++ : i;
        keys.set(i, key);
        values.set(i, value);
        tail %= keys.size();
    }

    // Just punch hole
    public void del(K key) {
        int i = keys.indexOf(key);
        if (-1 != i) {
            keys.set(i, null);
            values.set(i, null);
            //use this empty cell ?
            tail = i;
        }
    }
    
    public void clear() {
        for (int i = 0; i < keys.size(); i++) {
            keys.set(i, null);
            values.set(i, null);
        }
        tail = 0;
    }
    
    public int size() {
        return keys.size();
    }

    // TODO: throws IndexOutOfBoundsException
    public K at(int i) {
        return keys.get(i);
    }

    public Iterator<K> iterator() {
        return keys.iterator();
    }
}
