package com.qianyi.casinoweb.config;

import com.qianyi.modulecommon.util.Threads;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * @author ruoyi
 **/
@Configuration
public class ThreadPoolConfig
{
    // 核心线程池大小
//    private int corePoolSize = 50;
//
//
//    /**
//     * 执行周期性或定时任务
//     */
//    @Bean(name = "scheduledExecutorService")
//    protected ScheduledExecutorService scheduledExecutorService()
//    {
//        return new ScheduledThreadPoolExecutor(corePoolSize,
//                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build(),
//                new ThreadPoolExecutor.CallerRunsPolicy())
//        {
//            @Override
//            protected void afterExecute(Runnable r, Throwable t)
//            {
//                super.afterExecute(r, t);
//                Threads.printException(r, t);
//            }
//        };
//    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(30);
        return taskScheduler;
    }

}
