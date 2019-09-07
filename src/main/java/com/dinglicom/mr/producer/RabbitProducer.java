package com.dinglicom.mr.producer;

import com.dinglicom.mr.entity.correlationdata.AllObject;
import com.dinglicom.mr.producer.confirm.DDIBConfirm;
import com.dinglicom.mr.producer.confirm.RcuConfirm;
import com.dinglicom.mr.util.ChannelUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.java.Log;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;


@Log
@Component
public class RabbitProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    final RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {
        @Override
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            if (correlationData.getId() == null){
                return;
            }
            log.info("correlationData is" + correlationData.toString());
            String id = correlationData.getId();
            ObjectMapper objectMapper =new ObjectMapper();
            AllObject allObject = null;
            try {
                allObject = objectMapper.readValue(id, AllObject.class);
            } catch (IOException e) {
                log.info(e.getMessage());
            }
            if (allObject.getId().indexOf("RCU")>0){
                RcuConfirm rcuConfirm = new RcuConfirm();
                try {
                    rcuConfirm.RcuRetryAndUpdate(id,ack);
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            }
            if (allObject.getId().indexOf("DDIB")>0){
                DDIBConfirm ddibConfirm = new DDIBConfirm();
                try {
                    ddibConfirm.DDIBRetryAndUpdate(id,ack);
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
    }

    //发送消息方法调用: 构建自定义对象消息
    public synchronized void send(AllObject indexId, String listString, int priority) throws Exception {
        Channel channel = ChannelUtils.getChannel(rabbitTemplate);
        int messageCount = channel.queueDeclarePassive("dinglicom-queue").getMessageCount();
        log.info("-------------" + messageCount);
        if (100000 - messageCount < 100){
            return;
        }
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
        log.info("+++++++++++++++"+s+"+++++++++++++++++++++");
        rabbitTemplate.convertAndSend("dinglicom.topic", "dinglicom.rcu", listString, messagePostProcessorInner, correlationData);
    }
}
