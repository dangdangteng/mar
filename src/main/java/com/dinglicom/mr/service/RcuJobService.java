package com.dinglicom.mr.service;

import com.dingli.DecodeConfig;
import com.dingli.damain.TaskRequest;
import com.dinglicom.mr.entity.SourceFile;
import com.dinglicom.mr.entity.TaskConfig;
import com.dinglicom.mr.entity.correlationdata.AllObject;
import com.dinglicom.mr.producer.RabbitProducer;
import com.dinglicom.mr.repository.TaskConfigRepository;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.util.ObjectToObjectUtils;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

@Log
@Service
public class RcuJobService {
    private Logger logger = LoggerFactory.getLogger(RcuJobService.class);
    @Autowired
    private RabbitProducer rabbitProducer;
    @Autowired
    private TaskConfigRepository taskConfigRepository;

    public MessageCode rcu(int id, String filePathName, int port, Integer priority) throws Exception {
        Optional<TaskConfig> byId = taskConfigRepository.findById(1);
        SourceFile file = new SourceFile(id,filePathName, port);
        TaskRequest taskRequest = ObjectToObjectUtils.sourceFileToTaskRequest(file, byId.get());
        if (taskRequest == null) {
            return new MessageCode(0, "taskconfig : 配置错误!");
        }
        ArrayList arrayList = new ArrayList();
        DecodeConfig decodeConfig = new DecodeConfig();
        decodeConfig.setConfig(byId.get().getTemplatePath(), byId.get().getConfigPath());
        boolean b = decodeConfig.getDecodeRequestFile(taskRequest, arrayList);
        log.info("-----------" + b);
        if (b) {
            arrayList.stream().forEach(o -> {
                try {
                    AllObject allObject = new AllObject();
                    allObject.setId("RCU:"+id);
                    allObject.setFilePathName(filePathName);
                    allObject.setPort(port);
                    allObject.setObj(o + ":" + "RCU|" + id);
                    log.info(allObject.toString() + "--------------------");
                    rabbitProducer.send(allObject,   o + ":" + "RCU|" + id, priority == null ? 10 : priority);
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            });
            return new MessageCode(1, "解码成功! 数据已经压入队列...");
        }
        return new MessageCode(0, "未知错误: " + logger.toString());
    }
    public void retryRcu(AllObject allObject) throws Exception{
        int num = allObject.getNum();
        int i = num + 1;
        allObject.setNum(1);
        rabbitProducer.send(allObject,allObject.getObj(), 0);
    }
}
