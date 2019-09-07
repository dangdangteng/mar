package com.dinglicom.mr.config;

import com.dinglicom.mr.constant.Constants;
import com.dinglicom.mr.task.TestTask;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail testTaskQuartz(){
        return JobBuilder.newJob(TestTask.class).withIdentity("testTask").storeDurably().build();
    }
    @Bean
    public Trigger testQuartzTrigger1() {
        //5秒执行一次
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(Constants.sec)
                .repeatForever();
        return TriggerBuilder.newTrigger().forJob(testTaskQuartz())
                .withIdentity("testTask")
                .withSchedule(scheduleBuilder)
                .build();
    }

}
