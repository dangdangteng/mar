package com.dinglicom.mr.producer;

import com.dinglicom.mr.constant.Constants;
import com.dinglicom.mr.entity.correlationdata.AllObject;
import com.dinglicom.mr.producer.confirm.DDIBConfirm;
import com.dinglicom.mr.producer.confirm.RcuConfirm;
import com.dinglicom.mr.producer.confirm.ReportConfirm;
import com.dinglicom.mr.util.ChannelUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.java.Log;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;


@Log
@Component
public class RabbitProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Channel channel;

    /**
     * 插入队列设置锁，当队列满的时候不再插入数据
     */
    private ReentrantLock lock = new ReentrantLock();

    final RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {
        @Override
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            if (correlationData.getId() == null) {
                return;
            }
            log.info("correlationData is" + correlationData.toString());
            String id = correlationData.getId();
            ObjectMapper objectMapper = new ObjectMapper();
            AllObject allObject = null;
            try {
                allObject = objectMapper.readValue(id, AllObject.class);
            } catch (IOException e) {
                log.info(e.getMessage());
            }
            if (allObject.getId().indexOf("CU") > 0) {
                RcuConfirm rcuConfirm = new RcuConfirm();
                try {
                    rcuConfirm.RcuRetryAndUpdate(id, ack);
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            }
            if (allObject.getId().indexOf("DIB") > 0) {
                DDIBConfirm ddibConfirm = new DDIBConfirm();
                try {
                    ddibConfirm.dDIBRetryAndUpdate(id, ack);
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            }
            if (allObject.getId().indexOf("EPORT") > 0) {
                ReportConfirm reportConfirm = new ReportConfirm();
                try {
                    reportConfirm.ReportRetryAndUpdate(id, ack);
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            }
        }
    };

    @PostConstruct
    public void init() {
        // 通过实现 ConfirmCallback 接口，消息发送到 Broker 后触发回调，确认消息是否到达 Broker 服务器，也就是只确认是否正确到达 Exchange 中
        rabbitTemplate.setConfirmCallback(confirmCallback);
        channel = rabbitTemplate.getConnectionFactory().createConnection().createChannel(false);
        log.info("channel = " + channel.toString());
    }

    /**
     * 发送消息方法调用: 构建自定义对象消息
     *
     * @param indexId    任务ID
     * @param listString 监听绑定信息
     * @param priority   任务权重
     * @param high       任务级别，这里用来区分给级别和低级别，如果是同一接口调用，全部变成阻塞，高级别无法压入
     * @throws Exception
     */
    @Async
    public void send(AllObject indexId, String listString, int priority, boolean high) throws Exception {
        log.info("高级别报表任务send");
        if (channel == null) {
            channel = ChannelUtils.getChannel(rabbitTemplate);
            log.info("高级别报表任务 channel重新new------------- channel = " + channel);
        }
//        lock.lock();
        try {
            int messageCount;
            while (Constants.QUEUE_HIGH_PROPRITY_MAX == (messageCount = channel.queueDeclarePassive("dinglicom-queue").getMessageCount())) {
                log.info("高级别报表任务 等待中------------- messageCount = " + messageCount);
//                lock.wait();
            }
            convertAndSend(indexId, listString, priority);
        } catch (Exception e) {
            log.info("高级别报表任务 +++++++++++++++lock error" + e.getMessage() + "+++++++++++++++++++++");
        }
//        finally {
//            lock.unlock();
//        }
    }

    /**
     * 发送消息方法调用: 构建自定义对象消息
     *
     * @param indexId    任务ID
     * @param listString 监听绑定信息
     * @param priority   任务权重
     * @throws Exception
     */
    public void send(AllObject indexId, String listString, int priority) throws Exception {
//        Channel channel = ChannelUtils.getChannel(rabbitTemplate);
//        int messageCount = channel.queueDeclarePassive("dinglicom-queue").getMessageCount();
        if (channel == null) {
            channel = ChannelUtils.getChannel(rabbitTemplate);
        }
        lock.lock();
        log.info("send  common 上锁  +++++++++++++++++++++");
        try {
            int messageCount;
            while (Constants.QUEUE_COMMON_PROPRITY_MAX == (messageCount = channel.queueDeclarePassive("dinglicom-queue").getMessageCount())) {
                log.info("等待中。。。。。。。messageCount = " + messageCount);
                lock.wait();
            }
            convertAndSend(indexId, listString, priority);
            log.info("888888888888");
        } catch (Exception e) {
            log.info("+++++++++++++++lock error" + e.getMessage() + "+++++++++++++++++++++");
        } finally {
            lock.unlock();
            log.info("send  common 解锁  +++++++++++++++++++++");
        }
    }

    /**
     * Rabbit 发送消息以及发送之前的消息构建
     *
     * @param indexId
     * @param listString
     * @param priority
     * @throws Exception
     */
    private void convertAndSend(AllObject indexId, String listString, int priority) throws Exception {
        MessagePostProcessor messagePostProcessorInner = message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            //设置编码
            messageProperties.setContentEncoding("utf-8");
            //设置优先级
            messageProperties.setPriority(priority);
            return message;
        };
        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.writeValueAsString(indexId);
        CorrelationData correlationData = new CorrelationData(s);
        log.info("+++++++++++++++" + s + "+++++++++++++++++++++");
        rabbitTemplate.convertAndSend("dinglicom.topic", "dinglicom.rcu", listString, messagePostProcessorInner, correlationData);
        log.info("+++++++++++++++" + "bilibili" + "+++++++++++++++++++++");
    }

}
