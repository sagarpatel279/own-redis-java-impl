package components.repos;

import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class ExpiringMap <K,V>{
//    private final ConcurrentMap<K,V> concurrentMap=new ConcurrentHashMap<>();
//    private final ScheduledExecutorService scheduler= Executors.newScheduledThreadPool(1);
//    public void put(K key, V value,long delay, TimeUnit timeUnit){
//        concurrentMap.put(key,value);
//        scheduler.schedule(()->concurrentMap.remove(key),delay,timeUnit);
//    }
//    public void put(K key, V value){
//        concurrentMap.put(key,value);
//    }
//    public V get(K key){
//        return concurrentMap.get(key);
//    }
//    public boolean containsKey(K key){
//        return concurrentMap.containsKey(key);
//    }
private final ConcurrentMap<K, V> map = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void put(K key, V value, long delay, TimeUnit unit) {
        map.put(key, value);
        scheduler.schedule(() -> map.remove(key), delay, unit);
    }

    public void put(K key, V value) {
        map.put(key, value);
    }

    public V get(K key) {
        return map.get(key);
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

}
