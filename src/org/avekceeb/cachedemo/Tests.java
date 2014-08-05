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
    long threadTime = Cacheable.Minute / 2;
    long methodTime;

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
        System.out.println("[CacheDemo: starting test]");
    }

    @AfterMethod
    public void tearDown() {
        System.out.println("[CacheDemo: finished]");
    }

    // Cache strategies and cache sizes
    @DataProvider
    public Object[][] dataForSanity() {
        return new Object[][] {
            { "fifo", smallCacheSize }, 
            { "lru", smallCacheSize } 
        };
    }

    // Caches filled with data
    @DataProvider
    public Object[][] dataForMultithreadSanity() {
        Cacheable<Object, Object> fifo =
            new CacheBuilder<Object, Object>().createCache("fifo", cacheSize,
threadTime * 1000);
        Cacheable<Object, Object> lru =
                new CacheBuilder<Object, Object>().createCache("lru", cacheSize,
threadTime * 1000);
        for (int i=0; i<cacheSize; i++) {
            fifo.put(i, data.get(i));
            lru.put(i, data.get(i));
        }
        return new Object[][] {
                {fifo},
                {lru}
        };
    }
    @Test (dataProvider = "dataForSanity")
    public void testSanity(String strategy, int length) {
        Cacheable<Object, Object> c =
            new CacheBuilder<Object, Object>().createCache(strategy, length, 30 * Cacheable.Minute);
        Assert.assertNotNull(c);
        Assert.assertNull(c.get(rnd.nextInt()));
        for (int i=0; i<length; i++) {
            c.put(i, String.valueOf(i));
        }
        for (int i=0; i<length; i++) {
            Assert.assertEquals(c.get(i), String.valueOf(i));
        }
        System.out.println(c.toString());
    }

       @Test (dataProvider = "dataForSanity")
    public void testExpiry(String strategy, int length) {
        // ignore size and ttl:
        length = 2;
        long ttl = 15 * Cacheable.Second;
        Cacheable<Object, Object> c = 
            new CacheBuilder<Object, Object>().createCache(strategy, length, ttl);
        Assert.assertNotNull(c);
        c.put("c",3);
        System.out.println(c.toString());
        Assert.assertEquals(c.get("c"), 3);
        // check that cache will expire after ttl time;
        try {
            Thread.sleep(Cacheable.Minute);
        } catch (Exception e) {
        }
        Assert.assertNull(c.get("c"));
        System.out.println(c.toString());
        }

    @Test(dataProvider = "dataForMultithreadSanity",
            threadPoolSize = 5,
            singleThreaded=false,
            //invocationTimeOut=1
            invocationCount=5
            )
    public void testMitithreadSanity (Cacheable<Object,Object> c) {
        long timeToStop = System.currentTimeMillis() + threadTime;
        while (System.currentTimeMillis() < timeToStop) {
            for (Object k : data.keySet()) {
                Object v = c.get(k);
                // Ensure we are getting right value or cache miss:
                Assert.assertTrue((data.get(k).equals(v)) || (null == v));
            }
        }
    }

    private void doQuery(Cacheable<Object,Object> c, Object key) throws InterruptedException {
        Object v = c.get(key);
        Object expected = data.get(key);
        if (null == v) {
            // cache miss: TODO sleep (simulating request to Slow Storage)
            //Thread.sleep(50);
            c.put(key, expected);
        } else {
            Assert.assertEquals(v, expected);
        }
    }
    @Test(dataProvider = "dataForMultithreadSanity")
    public void testStatistic(Cacheable<Object,Object> c) throws InterruptedException {
        long start = System.currentTimeMillis();
        System.out.println("[CacheDemo.Statistics]");
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
}
