package repos;

import java.util.concurrent.*;

public class ExpiringMap <K,V>{
    private final ConcurrentMap<K,V> concurrentMap=new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler= Executors.newScheduledThreadPool(1);
    public void put(K key, V value,long delay, TimeUnit timeUnit){
        concurrentMap.put(key,value);
        scheduler.schedule(()->concurrentMap.remove(key),delay,timeUnit);
    }
    public void put(K key, V value){
        concurrentMap.put(key,value);
    }
    public V get(K key){
        return concurrentMap.get(key);
    }
    public boolean containsKey(K key){
        return concurrentMap.containsKey(key);
    }
}
