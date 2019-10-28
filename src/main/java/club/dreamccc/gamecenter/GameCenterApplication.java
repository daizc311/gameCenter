package club.dreamccc.gamecenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableAsync
public class GameCenterApplication {


    public static void main(String[] args) {
        SpringApplication.run(GameCenterApplication.class, args);
    }

    public static final String CACHE_NAME = "test";


    @Bean("ffmpegTheadPool")
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(1000);
        executor.setThreadNamePrefix("ffmpeg-");

        return executor;
    }

}
