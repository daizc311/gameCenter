package com.example.demo.async;

import com.example.demo.DemoApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class AsyncServiceImpl {

    @Autowired
    CachePool cachePool;


    @SuppressWarnings("All")
    @Async
    public void runTask(String asyncId) throws Exception {

        TaskRunnable taskRunnable = new TaskRunnable(asyncId);

        cachePool.getCacheMap(DemoApplication.CACHE_NAME).put(asyncId, taskRunnable);

        taskRunnable.run();

    }

    public void stopTask(String asyncId) throws Exception {

        ConcurrentHashMap<String, TaskRunnable> cacheMap = cachePool.getCacheMap(DemoApplication.CACHE_NAME);
        TaskRunnable task = cacheMap.get(asyncId);

        task.stop();
        cacheMap.remove(asyncId);
    }
}
