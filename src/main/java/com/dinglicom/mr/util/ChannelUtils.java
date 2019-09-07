package com.dinglicom.mr.util;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChannelUtils {
    /**
     * 获取channel
     * @param rabbitTemplate
     * @return
     * @throws IOException
     */
    public static Channel getChannel(RabbitTemplate rabbitTemplate) throws IOException {
        ConnectionFactory connectionFactory = rabbitTemplate.getConnectionFactory();
        Connection connection = connectionFactory.createConnection();
        Channel channel = connection.createChannel(false);
        return channel;
    }
    public static void getQueue(RabbitTemplate rabbitTemplate) throws Exception{
        ConnectionFactory connectionFactory = rabbitTemplate.getConnectionFactory();
        Connection connection = connectionFactory.createConnection();
        Channel channel = connection.createChannel(false);
        channel.queueDeclare("baiyangtest",true,false,false,mapPro());
    }
    private static Map mapPro(){
        Map map = new HashMap(16);
        map.put("x-max-length",100000);
        map.put("x-max-priority",100);
        return map;
    }
}
