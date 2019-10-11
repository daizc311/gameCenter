package com.example.demo;

import com.example.demo.async.CachePool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@EnableAsync
public class DemoApplication {


    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    public static final String CACHE_NAME = "test";

    @Bean
    public CachePool cachePool() {

        CachePool cachePool = new CachePool();
        try {
            ConcurrentHashMap<String, Boolean> test = cachePool.createCacheMap(CACHE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cachePool;
    }

    @Bean("ffmpegTheadPool")
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(1000);
        executor.setThreadNamePrefix("ffmpeg-");

        return executor;
    }

}
