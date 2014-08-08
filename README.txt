Задание:
------------
... необходимо реализовать кэш с FIFO и LRU...,
составить эти два типа стратегий
и написать тесты, проверяющие их работоспособность

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
    // простое решение: запись всегда в tail
    
LRUTable - HashMap + дополнительная Queue для хранения запрошенных ключей
    при каждом запросе ключ добавляется в конец очереди (и удаляется из середины если был)
    таким образом в голове очереди находятся самые "старые" запросы

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
    Процент попаданий для LRU на ~ 10% больше
        Эта разница зависит от распределения частоты ключей,
        а также от относительных размеров кеша

Перформанс для FIFO get получился слабый
    (это проблема моей реализации, а не алгоритма)
.......get....put....rem..
FIFO...0,5M...500M...570M.
LRU....5M.....230M...200M.



org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.FIFOTable> ttl=60000000ms size=2000 requests=0 hits=0.00% ]
Get (Ops/sec): 550964]
Put (Ops/sec): 511182108]
Remove (Ops/sec): 571428571]
 org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.LRUTable> ttl=60000000ms size=2000 requests=0 hits=0.00% ]
Get (Ops/sec): 5000000]
Put (Ops/sec): 230215827]
Remove (Ops/sec): 200000000]
 org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.FIFOTable> ttl=1800000ms size=8000 requests=0 hits=0.00% ]
Put String (Ops/sec): 27874]
Get String (Ops/sec): 33755]
Remove String (Ops/sec): 242424]
 org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.LRUTable> ttl=1800000ms size=8000 requests=0 hits=0.00% ]
Put String (Ops/sec): 26845]
Get String (Ops/sec): 4000000]
Remove String (Ops/sec): 1600000]


Тестирование:
------------
    Использован TestNG
    3 вида тестов: функциональные, перформанс и конкаренси

# ant

test.functional:
   [testng] [ org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.FIFOTable> ttl=1800000ms size=10 requests=10 hits=100.00% ]
   [testng] [ org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.LRUTable> ttl=1800000ms size=10 requests=10 hits=100.00% ]
   [testng] ===============================================
   [testng]     Functional
   [testng]     Tests run: 4, Failures: 0, Skips: 0
   [testng] ===============================================

test.benchmark:
   [testng] [ org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.FIFOTable> ttl=60000000ms size=2000 requests=0 hits=0.00% ]
   [testng] [Get (Ops/sec): 550964]
   [testng] [Put (Ops/sec): 511182108]
   [testng] [Remove (Ops/sec): 571428571]
   [testng] [ org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.LRUTable> ttl=60000000ms size=2000 requests=0 hits=0.00% ]
   [testng] [Get (Ops/sec): 5000000]
   [testng] [Put (Ops/sec): 230215827]
   [testng] [Remove (Ops/sec): 200000000]
   [testng] [ org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.FIFOTable> ttl=1800000ms size=8000 requests=0 hits=0.00% ]
   [testng] [Put String (Ops/sec): 27874]
   [testng] [Get String (Ops/sec): 33755]
   [testng] [Remove String (Ops/sec): 242424]
   [testng] [ org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.LRUTable> ttl=1800000ms size=8000 requests=0 hits=0.00% ]
   [testng] [Put String (Ops/sec): 26845]
   [testng] [Get String (Ops/sec): 4000000]
   [testng] [Remove String (Ops/sec): 1600000]
   [testng] [CacheDemo.Statistics]
   [testng] [ org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.FIFOTable> ttl=60000000ms size=2000 requests=35000 hits=70.59% ]
   [testng] [Elapsed: 123 ms]
   [testng] [CacheDemo.Statistics]
   [testng] [ org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.LRUTable> ttl=60000000ms size=2000 requests=35000 hits=81.68% ]
   [testng] [Elapsed: 259 ms]
   [testng] ===============================================
   [testng]     Benchmark
   [testng]     Tests run: 6, Failures: 0, Skips: 0
   [testng] ===============================================

test.stability:
    ....
   [testng] [Thread #18: gets=6504 puts=3303 removes=1312 clears=641]
    ....
   [testng] PASSED: testMultithreadSanity([ org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.FIFOTable> ttl=60000000ms size=2000 requests=18408000 hits=25.00% ])
    ....
   [testng] PASSED: testMultithreadSanity([ org.avekceeb.cachedemo.Cache<org.avekceeb.cachedemo.LRUTable> ttl=60000000ms size=2000 requests=1936520000 hits=25.00% ])
    ....
   [testng] ===============================================
   [testng]     Stability
   [testng]     Tests run: 26, Failures: 0, Skips: 0
   [testng] ===============================================

BUILD SUCCESSFUL
Total time: 4 minutes 26 seconds

