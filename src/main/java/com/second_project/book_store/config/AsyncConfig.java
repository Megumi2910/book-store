package com.second_project.book_store.config;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration for asynchronous task execution.
 * 
 * Enables @Async annotation support and configures thread pool for async operations.
 * 
 * Use cases in this application:
 * - Email sending (don't block HTTP request while sending emails)
 * - Background cleanup tasks
 * - Event processing
 * 
 * BEST PRACTICES:
 * - Configure thread pool size based on expected load
 * - Set queue capacity to prevent unbounded queue growth
 * - Use meaningful thread names for debugging
 * - Handle exceptions properly (AsyncUncaughtExceptionHandler)
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Configure the executor for @Async methods.
     * 
     * Thread Pool Configuration:
     * - Core pool size: 5 threads (always kept alive)
     * - Max pool size: 10 threads (grows when queue is full)
     * - Queue capacity: 100 (holds pending tasks)
     * - Thread name prefix: "async-" (for easy identification in logs)
     * 
     * Sizing guidelines:
     * - For I/O-bound tasks (emails, API calls): Higher thread count (5-20)
     * - For CPU-bound tasks: Lower thread count (num_cores)
     * - Queue capacity: 2-3x max pool size
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core threads are always kept alive
        executor.setCorePoolSize(5);
        
        // Maximum threads that can be created
        executor.setMaxPoolSize(10);
        
        // Queue capacity for pending tasks
        // When queue is full and max threads reached, tasks will be rejected
        executor.setQueueCapacity(100);
        
        // Thread names for debugging
        executor.setThreadNamePrefix("async-");
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // Max time to wait for tasks on shutdown (30 seconds)
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        
        logger.info("Async executor configured: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                   executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }

    /**
     * Handle uncaught exceptions from @Async methods.
     * These exceptions won't propagate to the caller since async methods run in separate threads.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            logger.error("Uncaught exception in async method '{}': {}", 
                        method.getName(), throwable.getMessage(), throwable);
            logger.error("Method parameters: {}", java.util.Arrays.toString(params));
        };
    }
}

