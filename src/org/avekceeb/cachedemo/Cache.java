package org.avekceeb.cachedemo;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;

public class Cache<K,V> implements Cacheable<K,V> {
	protected int _size;
	protected long _ttl; // Time To Live (time-to-die actually)
	protected int _misses;
	protected int _hits;
	//protected int _vacancies;
	protected CacheTable<K,CacheEntry<V>> table;
    protected TimerTask ticker;
    protected Timer timer;
    class Ticker extends TimerTask {
        public void run() {
        	if (null == table)
                return;
    		ArrayList<K> forRemoval = new ArrayList<>();
            for (K k : table) {
                CacheEntry<V> e = table.get(k);
                if (null != e) {
                    if ( e.ttl < System.currentTimeMillis() ) {
                        forRemoval.add(k);
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
		//_vacancies = size;
		table = t;
        // TODO: set limits
        long timeFactor = ttl * 2 / size;
        timer = new Timer();
        ticker = new Ticker();
        timer.schedule(ticker, timeFactor, timeFactor);
	}

	public int size() {
		return _size;
	}
	
    public String toString() {
    	int req = _hits + _misses;
        String s = this.getClass().getName() +
                " <" + table.getClass().getName() + ">" +
        		" TTL=" + _ttl + " Size=" + _size +
        		" Hits=" + _hits + " Misses=" + _misses + 
        		" hits ratio=" + ((0 == req) ? "?" : 100*_hits/req) + "%";
        return s;
    }
    
    public String dumpKeys() {
    	String s = "\n";
        for (K k : table) {
        	CacheEntry<V> e = table.get(k);
        	s += k + ":" + ((null == e) ? e : e.value) + " ";
        }
        return s + "\n";
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
    	//synchronized(table) {
        table.put(key, new CacheEntry<V>(value, System.currentTimeMillis() + _ttl));
    	//}
    }    
    
	public void remove(Object key) {}

	public void clear() {}

}
