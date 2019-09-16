package com.example.demo;

import com.example.demo.async.CachePool;
import com.example.demo.ffmpeg.FFmpegPublish;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@EnableAsync
public class DemoApplication {

    @Autowired
    RedisTemplate redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public RedisTemplate redisTemplateInit() {
        //设置序列化Key的实例化对象
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //设置序列化Value的实例化对象
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
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
