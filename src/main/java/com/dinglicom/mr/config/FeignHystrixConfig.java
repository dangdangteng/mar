package com.dinglicom.mr.config;

import com.dinglicom.mr.constant.Constants;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import feign.Feign;
import feign.Request;
import feign.Retryer;
import feign.Target;
import feign.hystrix.HystrixFeign;
import feign.hystrix.SetterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

@Configuration
public class FeignHystrixConfig {


    @Bean
    public Feign.Builder feignHystrixBuilder() {
        return HystrixFeign.builder()
                .setterFactory(
                        new SetterFactory() {
                            @Override
                            public HystrixCommand.Setter create(Target<?> target, Method method) {
                                return HystrixCommand.Setter.withGroupKey(
                                        HystrixCommandGroupKey.Factory.asKey(
                                                CloudUnifyRedisFeign.class.getSimpleName()))
                                        .andCommandPropertiesDefaults(
                                                HystrixCommandProperties.Setter()
                                                        .withExecutionTimeoutInMilliseconds(10000)
                                                        .withExecutionIsolationThreadInterruptOnTimeout(true)
                                                        .withRequestCacheEnabled(true)
                                        )
                                        .andThreadPoolPropertiesDefaults(
                                                HystrixThreadPoolProperties.Setter()
                                                        .withCoreSize(1000)
                                                        .withMaxQueueSize(1000)
                                                        .withKeepAliveTimeMinutes(60)
                                                        .withQueueSizeRejectionThreshold(500));
                            }
                        });
    }

    @Bean
    public Request.Options options() {
        return new Request.Options(Constants.connectTimeOutMillis, Constants.readTimeOutMillis);
    }

    @Bean
    public Retryer feignRetryer() {
        Retryer retryer = new Retryer.Default(100, 1000, 4);
        return retryer;
    }
}
