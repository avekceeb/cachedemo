package org.avekceeb.cachedemo;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

import java.util.Random;
import java.util.HashMap;

public class Tests {

    Random rnd = new Random();
    int getters = 10;
    int setters = 10;
    int rounds = 1000;
    int smallCacheSize = 10;
    int cacheSize = 2000;
    int dataSize = cacheSize * 40;
    // fixed amount of "hot" request:
    int frequentDataSize = cacheSize/2;
    HashMap<Object, Object> data = new HashMap<>(dataSize);
    long threadTime = Cacheable.Minute;

    @BeforeClass
    public void init() {
        Assert.assertTrue(frequentDataSize < dataSize);
        //Assert.assertTrue(cacheSize < dataSize);
        for (int i=0; i<dataSize; i++) {
            data.put(i, rnd.nextInt());
        }
    }

    @BeforeMethod
    public void setUp() {
    }

    @AfterMethod
    public void tearDown() {
    }

    // Cache strategies and cache sizes
    @DataProvider
    public Object[][] dataForSanity() {
        return new Object[][] {
            { "fifo", smallCacheSize }, 
            { "lru", smallCacheSize } 
        };
    }

    public Object[][] createCaches(int length, long ttl) {
        Cacheable<Object, Object> fifo =
            new CacheBuilder<Object, Object>().
            	createCache("fifo", length, ttl);
        Cacheable<Object, Object> lru =
                new CacheBuilder<Object, Object>().
                	createCache("lru", length, ttl);
        for (int i=0; i<length; i++) {
            fifo.put(i, data.get(i));
            lru.put(i, data.get(i));
        }
        return new Object[][] {
                {fifo},
                {lru}
        };
    }

    
    // Caches filled with data
    @DataProvider
    public Object[][] dataForMultithreadSanity() {
        return createCaches(cacheSize, threadTime * 1000);
    }

    @DataProvider /*(parallel=true)*/
    public Object[][] dataForStability() {
        return createCaches(cacheSize, threadTime / 4);
    }

    @Test (dataProvider = "dataForSanity", groups="sanity")
    public void testSanity(String strategy, int length) {
        Cacheable<Object, Object> c =
            new CacheBuilder<Object, Object>().createCache(strategy, length, 30 * Cacheable.Minute);
        Assert.assertNotNull(c);
        Assert.assertNull(c.get(rnd.nextInt()));
        c.clear();
        c.remove(123);
        for (int i=0; i<length; i++) {
            c.put(i, String.valueOf(i));
        }
        for (int i=0; i<length; i++) {
            Assert.assertEquals(c.get(i), String.valueOf(i));
        }
        c.put(null, 123);
        Assert.assertNull(c.get(null));
        System.out.println(c.toString());
    }
    
    /*
     * Tests that cache items expire after period set
     */
    @Test (dataProvider = "dataForSanity")
    public void testExpiry(String strategy, int length) throws InterruptedException {
        long ttl = 5 * Cacheable.Second;
        Cacheable<Object, Object> c = 
            new CacheBuilder<Object, Object>().createCache(strategy, length, ttl);
        Assert.assertNotNull(c);
        c.put("c", 3);
        Assert.assertEquals(c.get("c"), 3);
        // check that cache will expire after ttl*2 time;
        Thread.sleep(ttl * 2);
        Assert.assertNull(c.get("c"));
    }

    /*
     * Tests concurrent get operations in tight loop
     */
    @Test(dataProvider = "dataForMultithreadSanity",
    		threadPoolSize = 3,
            invocationCount = 3)
    public void testMultithreadSanity (Cacheable<Object,Object> c) {
        long timeToStop = System.currentTimeMillis() + threadTime;
        while (System.currentTimeMillis() < timeToStop) {
            for (Object k : data.keySet()) {
                Object v = c.get(k);
                // Ensure we are getting right value or cache miss:
                Assert.assertTrue((null == v) || (data.get(k).equals(v)));
            }
        }
    }

    private void doQuery(Cacheable<Object,Object> c, Object key) {
        Object v = c.get(key);
        Object expected = data.get(key);
        if (null == v) {
            c.put(key, expected);
        } else {
            Assert.assertEquals(v, expected);
        }
    }
    /*
     * Examines Cache strategies in a single threaded tight loop
     * of querying cache with requests
     * frequent : regular as 4:1
     */
    @Test(dataProvider = "dataForMultithreadSanity")
    public void testStatistic(Cacheable<Object,Object> c) throws InterruptedException {
        System.out.println("[CacheDemo.Statistics]");
        long start = System.currentTimeMillis();
        for (int i=frequentDataSize; i<dataSize; i++) {
            doQuery(c, i);
            for (int j=0; j<4; j++) {
                doQuery(c, rnd.nextInt(frequentDataSize));
            }
        }
        start = (System.currentTimeMillis() - start);
        System.out.println(c.toString());
        System.out.println("[Elapsed: " + start + " ms]");
    }
    
    @Test (dataProvider = "dataForMultithreadSanity")
    public void testBenchmark(Cacheable<Object,Object> c) {
        int length = c.size();
        System.out.println(c.toString());
        long start = System.currentTimeMillis();
        for (int i=0; i<100; i++) {
            for (int j=0; j<length; j++) {
                c.get(j);
            }
        }
        start = (System.currentTimeMillis() - start);
        System.out.format("[Get Ops/sec: %d]%n" , (100*Cacheable.Second*length)/start);

        start = System.currentTimeMillis();
        for (int i=0; i<10; i++) {
            for (int j=0; j<dataSize; j++) {
                c.put(j,j);
            }
        }
        start = (System.currentTimeMillis() - start);
        System.out.format("[Put (Ops/sec): %d]%n" , (dataSize*10*Cacheable.Second*length)/start);
    
        start = System.currentTimeMillis();
        for (int i=0; i<1; i++) {
            for (int j=0; j<length; j++) {
                c.remove(j);
            }
        }
        start = (System.currentTimeMillis() - start);
        System.out.format("[Remove (Ops/sec): %d]%n" , (length*Cacheable.Second*length)/start);

    }

    @Test (dataProvider = "dataForStability", threadPoolSize = 10, invocationCount = 10)
    public void testStability(Cacheable<Object,Object> c) throws InterruptedException {
    	long timeToStop = System.currentTimeMillis() + threadTime;
        int currentKey, guess, puts=0, gets=0, removes=0, clears=0;
        while (System.currentTimeMillis() < timeToStop) {
        	 guess = rnd.nextInt(100);
        	 currentKey = rnd.nextInt(dataSize);
             if (25 > guess) { /* 1:4 */
            	 c.put(currentKey, data.get(currentKey));
            	 puts++;
             }
             if (50 > guess) { /* 1:2*/
                c.get(currentKey);
                gets++;
             }
             if (10 > guess) { /* 1:10 */
                c.remove(currentKey);
                removes ++;
             }
             if (5 > guess) { /* 1:20 */
                c.clear();
                clears++;
             }
             Thread.sleep(rnd.nextInt(10));
        }
        System.out.format("[Thread #%d: gets=%d puts=%d removes=%d clears=%d]%n", Thread.currentThread().getId(), gets, puts, removes, clears);
    }

}
