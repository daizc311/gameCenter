package club.dreamccc.gamecenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GameCenterApplication {


    public static void main(String[] args) {
        SpringApplication.run(GameCenterApplication.class, args);
    }

}
