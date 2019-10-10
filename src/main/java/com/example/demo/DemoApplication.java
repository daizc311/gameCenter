package com.example.demo;

import com.example.demo.async.CachePool;
import com.example.demo.ffmpeg.FFmpegPublish;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@EnableAsync
public class DemoApplication {



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

    @Bean
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(1000);

//        ThreadPoolTaskExecutor executor2 = new ThreadPoolTaskExecutor();
//        executor2.set


        return executor;
    }

    @Bean(name = "ffmpegPulish")
    @Lazy
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FFmpegPublish ffmpegPulish() {

        return new FFmpegPublish();
    }
}
