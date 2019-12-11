package com.dinglicom.mr.task;

import com.dingli.cloudunify.core.response.Response;
import com.dinglicom.mr.config.HttpPool;
import com.dinglicom.mr.entity.JobMessageListenerEntity;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import com.dinglicom.mr.handle.HandleContext;
import com.dinglicom.mr.repository.JobMessageListenerRepository;
import lombok.extern.java.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Log
@Configuration
@EnableScheduling
public class ETListenEarth {
    /**
     * 没有绝对完善之事,单独使用则无虑,多则漏洞百出
     */
    @Autowired
    private JobMessageListenerRepository jobMessageListenerRepository;

    @Autowired
    private CloudUnifyRedisFeign cloudUnifyRedisFeign;

    @Autowired
    private HttpPool httpPool;

    @Autowired
    private HandleContext handleContext;

    @Scheduled(fixedRate = 60000 * 5)
    private void listenEarth() throws Exception{
        log.info("master: 我5分钟观察你一次.");
        log.info("work: 不要不要人家会害羞的....");
        HttpClient httpClient = httpPool.httpClient();
        //读数据库 做轮询发送http请求获取
        //是读取消息 移除 更新数据库状态
        Iterable<JobMessageListenerEntity> all = jobMessageListenerRepository.findAll();
        all.forEach(jobMessageListenerEntity -> {
            // 执行请求，相当于敲完地址后按下回车。获取响应
            try {
                String url = "http://" + jobMessageListenerEntity.getIworkIp() + ":" + jobMessageListenerEntity.getIworkPort() + "/actuator/info";
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = null;
                response = httpClient.execute(httpGet);
                if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 300) {
                    // 解析响应，获取数据
                    String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                    log.info("监听信息: " + content);
                } else {
                    // 监听不到 处理
                    // 获取key 移除redis 数据并判断任务类型下发 下一级任务
                    // 移除监听数据 放弃监听
                    String jobMessage = jobMessageListenerEntity.getJobMessage();
                    // 信道信息处理
                    String serviceId= jobMessage.substring(jobMessage.indexOf("|")+1,jobMessage.indexOf(":"));
                    try {
                        handleContext.doJobAtNow(serviceId, jobMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //todo:
                    Response<Boolean> remove = cloudUnifyRedisFeign.remove(jobMessage);
                    jobMessageListenerRepository.delete(jobMessageListenerEntity);
                }
            } catch (Exception e) {
                String jobMessage = jobMessageListenerEntity.getJobMessage();
                String serviceId = jobMessage.substring(jobMessage.indexOf("|") + 1, jobMessage.indexOf(":"));
                try {
                    handleContext.doJobAtNow(serviceId, jobMessage);
                } catch (Exception ez) {
                    ez.printStackTrace();
                }
                //todo:
                Response<Boolean> remove = cloudUnifyRedisFeign.remove(jobMessage);
                jobMessageListenerRepository.delete(jobMessageListenerEntity);
            }
        });
    }
}
