package com.dinglicom.mr.config;

import lombok.extern.java.Log;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Log
@Configuration
public class HttpPoolConfig {

    @Bean
    public HttpClient httpClient(){
        log.info("===== Apache httpclient 初始化连接池开始===" );
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setSocketTimeout(5 * 1000);
        requestConfigBuilder.setConnectTimeout(5 * 1000);
        RequestConfig defaultRequestConfig = requestConfigBuilder.build();
        final PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager(30, TimeUnit.MILLISECONDS);
        pollingConnectionManager.setMaxTotal(1000);
        pollingConnectionManager.setDefaultMaxPerRoute(100);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
        httpClientBuilder.setConnectionManager(pollingConnectionManager);
        httpClientBuilder.setDefaultRequestConfig(defaultRequestConfig);
        HttpClient client = httpClientBuilder.build();
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("httpclient-schedule-pool-%d").daemon(true).build());
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("=====closeIdleConnections===");
                pollingConnectionManager.closeExpiredConnections();
                pollingConnectionManager.closeIdleConnections(5, TimeUnit.SECONDS);
            }
        },10*1000,5, TimeUnit.HOURS);
        log.info("===== Apache httpclient 初始化连接池完毕===");
        return client;
    }
}