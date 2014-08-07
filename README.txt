Задание:
------------
...необходимо реализовать кэш с FIFO и LRU..., составить эти два типа стратегий и написать тесты, проверяющие их работоспособность

Реализация:
------------
Релизован класс Cache:

interface Cacheable<K,V> {
    public V get(K key);
    public void put(K key, V value);
    public void remove(K key);
    public void clear();
    public int size();
}

class Cache<K,V> implements Cacheable<K,V> {
    ...
    // структура данных реализующая конкретный алгоритм кэширования
    // передаётся при инстанцировании объекта
    protected CacheTable<K,CacheEntry<V>> table;
    ...
}

Реализованы две стратегии
class FIFOTable<K,V> implements CacheTable<K,V>
class LRUTable<K,V> implements CacheTable<K,V>

FIFOTable - это циклический "двойной" лист
    ArrayList<K> keys, values;
    private int tail;
    // простое решение
    
LRUTable - HashMap + дополнительная Queue для хранения запрошенных ключей

Инвалидация по истечении времени жизни ключа - в отдельной задаче
    зашедуленной при создании кэша

Недостатки:
------------
    - синхронизация целиком на таблицу на каждую модифицирующую операцию 
    - обработка исключений недоделана. (если что-то плохо - возвращается null)
    - реализация класса как generic - похоже излишняя
    - использование структур данных которые хранят только уникальные значения
        (деревья) возможно более оправдано для кэшей
    - нет resize

Сравнение:
------------
Процент попаданий для LRU на 10% больше


Тестирование:
------------
Использован TestNG


ant test

test.functional:
   [testng] ===============================================
   [testng]     Functional
   [testng]     Tests run: 4, Failures: 0, Skips: 0
   [testng] ===============================================

test.benchmark:
   [testng] [ org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.FIFOTable> ttl=60000000ms size=2000 requests=0 hits=0.00% ]
   [testng] [Get Ops/sec: 549450]
   [testng] [Put (Ops/sec): 512491992]
   [testng] [Remove (Ops/sec): 571428571]
   [testng] [ org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.LRUTable> ttl=60000000ms size=2000 requests=0 hits=0.00% ]
   [testng] [Get Ops/sec: 5555555]
   [testng] [Put (Ops/sec): 228212808]
   [testng] [Remove (Ops/sec): 181818181]
   [testng] [CacheDemo.Statistics]
   [testng] [ org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.FIFOTable> ttl=60000000ms size=2000 requests=395000 hits=66.38% ]
   [testng] [Elapsed: 1520 ms]
   [testng] [CacheDemo.Statistics]
   [testng] [ org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.LRUTable> ttl=60000000ms size=2000 requests=395000 hits=78.92% ]
   [testng] [Elapsed: 2923 ms]
   [testng] ===============================================
   [testng]     Benchmark
   [testng]     Tests run: 4, Failures: 0, Skips: 0
   [testng] ===============================================

test.stability:
   [testng] [ThreadUtil] Starting executor timeOut:0ms workers:3 threadPoolSize:3
   [testng] [ThreadUtil] Starting executor timeOut:0ms workers:10 threadPoolSize:10
   [testng] [Thread #26: gets=6532 puts=3300 removes=1307 clears=649]
....
   [testng] ===============================================
   [testng]     Stability
   [testng]     Tests run: 26, Failures: 0, Skips: 0
   [testng] ===============================================

Total time: 4 minutes 39 seconds


