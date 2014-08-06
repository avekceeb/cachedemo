package org.avekceeb.cachedemo;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;

public class Cache<K,V> implements Cacheable<K,V> {
    protected int _size;
    protected long _ttl;
    protected int _misses;
    protected int _hits;
    protected CacheTable<K,CacheEntry<V>> table;
    protected TimerTask ticker;
    protected Timer timer;
    class Ticker extends TimerTask {
        public void run() {
            ArrayList<K> forRemoval = new ArrayList<>();
            if (null == table)
                return;
            synchronized(table) {
	            for (K k : table) {
	                CacheEntry<V> e = table.get(k);
	                if (null != e) {
	                    if ( e.ttl < System.currentTimeMillis() ) {
	                        forRemoval.add(k);
	                    }
	                }
	            }
            }
            for (K i : forRemoval)
                table.del(i);
        }
    }

    protected Cache() { };

    public Cache(int size, long ttl, CacheTable<K,CacheEntry<V>> t) {
        _size = size;
        _ttl = ttl;
        table = t;
        // TODO: set limits
        long timeFactor = ttl / 2;
        timer = new Timer();
        ticker = new Ticker();
        timer.schedule(ticker, timeFactor, timeFactor);
    }

    public int size() {
        return _size;
    }

    public String toString() {
        return String.format("[ %s<%s> ttl=%dms size=%d requests=%d hits=%.2f%% ]",
                this.getClass().getName(),
                table.getClass().getName(),
                _ttl,  _size, _hits + _misses,
                ((0 == _hits+_misses) ? 0.0 : 100.0 * _hits/(_hits+_misses)));
    }

    public V get(K key) {
        CacheEntry<V> e = table.get(key);
        if (null != e) {
            _hits++;
            return e.value;
        }
        _misses++;
        return null;
    }

    public void put(K key,V value) {
        synchronized(table) {
            table.put(key, new CacheEntry<V>(value, System.currentTimeMillis() + _ttl));
        }
    }

    public void remove(K key) {
    	synchronized(table) {
    		table.del(key);
    	}
    }

    public void clear() {
    	synchronized(table) {
    		table.clear();
    		_hits = 0;
    		_misses = 0;
    	}
    }

}
