package com.example.demo.async;

import com.example.demo.DemoApplication;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TaskRunnable {


    private boolean isRun;

    private String taskName;

    public TaskRunnable(String taskName) {
        this.isRun = true;
        this.taskName = taskName;
    }

    private void checkRun() throws Exception {

        log.error("{} this.isRun={}", taskName, this.isRun);
    }

    public void run() throws Exception {

        log.error("run");

        int i = 0;
        while (i < 20 && isRun) {

            try {
                Thread.sleep(3000);
                checkRun();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
            if (i % 2 == 0) log.info("运行中，第[{}]秒", i * 3);
        }

        exit();
    }

    private void exit() throws Exception {

        log.error("exit");
    }

    public void stop() {
        this.isRun = false;
    }
}
