package org.avekceeb.cachedemo;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;

class LRUQueue<K> extends LinkedList<K>{
	private static final long serialVersionUID = 1L;
	private int size;
	@SuppressWarnings("unused")
	private LRUQueue() {}
	public LRUQueue (int length) {
		super();
		size = length;
	}
	public boolean add(K item) {
		super.removeFirstOccurrence(item);
		if (super.size() < size) {
			return super.add(item);
		} else {
			// queue is full. its OK. don't touch it
			return false;
		}
	}
}

public class LRUTable<K,V> implements CacheTable<K,V>  {
	private int size;
    private LRUQueue<K> lru;
    private HashMap<K, V> map;
    
	@SuppressWarnings("unused")
    private LRUTable() {}
    
    public LRUTable(int length) {
    	size = length;
    	lru = new LRUQueue<K>(size);
        map = new HashMap<K, V>(size /* loadfactor = 0.75 */);
    }
    
    public V get(K k) {
    	V value = map.get(k);
    	if (null != value) {
        	lru.add(k);
    	}
    	return value;
    }
    
    public void put(K k, V v) {
    	V old = map.get(k);
    	if (null == old) {
    		// no such an entry
    		if (map.size() < size) {
    			// there is room for new items
    			lru.add(k); // initial load
    		} else {
    			// no room for new items
    			K outdated = lru.poll();
    			if (null == outdated) {
    				throw new NullPointerException("No Room In Cache!");
    			}
    			lru.add(k);
    			map.remove(outdated);
    		}
    	}
		map.put(k, v);
    }
    
    public void del(K k) {
		lru.removeFirstOccurrence(k);
        map.remove(k);
    }
    
    public void clear() {
    	
    }
    
    public Iterator<K> iterator() {
    	return map.keySet().iterator();
    }
}




