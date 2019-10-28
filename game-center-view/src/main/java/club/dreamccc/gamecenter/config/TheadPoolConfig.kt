package club.dreamccc.gamecenter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
open class TaskTheadPoolConfig {

    @Bean("taskTheadPool")
    open fun taskExecutor(): AsyncTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 10
        executor.maxPoolSize = 100
        executor.threadNamePrefix = "task-"

        return executor
    }

}

@Configuration
open class TheadPoolConfig {

    @Bean("theadPool")
    open fun taskExecutor(): AsyncTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 10
        executor.maxPoolSize = 100
        executor.threadNamePrefix = "thead-"

        return executor
    }

}
