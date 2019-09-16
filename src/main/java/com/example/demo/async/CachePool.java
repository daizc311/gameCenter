package com.example.demo.async;

import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CachePool {

    private volatile static ConcurrentHashMap<String, ConcurrentHashMap> cacheMap = new ConcurrentHashMap<>();


    public static <K, V> ConcurrentHashMap<K, V> getCacheMap(String cacheMapName) throws Exception {

        ConcurrentHashMap<K, V> concurrentHashMap = cacheMap.get(cacheMapName);


        if (Objects.isNull(concurrentHashMap)){
            throw new Exception("1");
        }
        return concurrentHashMap;
    }

    public static <K, V> ConcurrentHashMap<K, V> createCacheMap(String key) throws Exception {

        ConcurrentHashMap<K, V> concurrentHashMap = cacheMap.get(key);


        if (Objects.isNull(concurrentHashMap)){
            concurrentHashMap = cacheMap.put(key,new ConcurrentHashMap<K, V>());
        }
        return concurrentHashMap;
    }

}
