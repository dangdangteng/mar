package com.dinglicom.mr.service;

import com.dingli.ImportDbConfig;
import com.dingli.damain.TaskRequest;
import com.dinglicom.mr.Enum.StatusEnum;
import com.dinglicom.mr.entity.DecodeFile;
import com.dinglicom.mr.entity.DecodeFileKidJob;
import com.dinglicom.mr.entity.TaskConfig;
import com.dinglicom.mr.entity.correlationdata.AllObject;
import com.dinglicom.mr.producer.RabbitProducer;
import com.dinglicom.mr.repository.DecodeFileKidJobRepository;
import com.dinglicom.mr.repository.TaskConfigRepository;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.util.ObjectToObjectUtils;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Log
@Service
public class DDiBJobService {
    private Logger logger = LoggerFactory.getLogger(DDiBJobService.class);
    @Autowired
    private RabbitProducer rabbitProducer;
    @Autowired
    private TaskConfigRepository taskConfigRepository;
    @Autowired
    private DecodeFileKidJobRepository decodeFileKidJobRepository;


    public MessageCode ddib(int id, String filePathName, int port, Integer priority) throws Exception {
        AllObject allObject = new AllObject();
        DecodeFileKidJob decodeFileKidJob = new DecodeFileKidJob();
        Optional<TaskConfig> byId = taskConfigRepository.findById(2);
        DecodeFile file = new DecodeFile(id, port, filePathName);
        log.info(decodeFileKidJob.toString() + "***************" + byId.get().toString());
        TaskRequest taskRequest = ObjectToObjectUtils.decodeFileToTaskRequest(file, byId.get());
        log.info("taskRequest :" + taskRequest);
        if (taskRequest == null) {
            return new MessageCode(0, "taskconfig : 配置错误!");
        }
        ArrayList arrayList = new ArrayList();
        ImportDbConfig importDbConfig = new ImportDbConfig();
        importDbConfig.setConfig(byId.get().getConfigPath(), byId.get().getTemplatePath(), byId.get().getConfigPath());
        importDbConfig.setDBConnectionConfig("172.16.23.152","db_1","root","Fleet2011@DB.","3306","0");
        log.info("success ----------------");
        boolean b = importDbConfig.getImportRequestFile(taskRequest, arrayList);
        log.info("-----------------" + b);
        if (b) {
            arrayList.parallelStream().forEach(o -> {
                try {
//                    <!-- 烦死了的逻辑 start-->
                    log.info("拆任务并发入库开始...");
                    decodeFileKidJob.setTaskId(id);
                    decodeFileKidJob.setLevel(1);
                    decodeFileKidJob.setRetryCount(0);
                    decodeFileKidJob.setState(1);
                    decodeFileKidJob.setStartTime(LocalDateTime.now());
                    decodeFileKidJob.setEndTime(LocalDateTime.now());
                    decodeFileKidJob.setException(StatusEnum.getMessage(1));
                    DecodeFileKidJob save = decodeFileKidJobRepository.save(decodeFileKidJob);
                    log.info("入库完毕..." + save.toString());

                    allObject.setId("DDIB:" + decodeFileKidJob.getId());
                    allObject.setPort(port);
                    allObject.setFilePathName(filePathName);
                    allObject.setObj(o + ":" + "DDIB|" + decodeFileKidJob.getId());
                    rabbitProducer.send(allObject, o + ":" + "DDIB|" + decodeFileKidJob.getId(), priority == null ? 20 : priority);
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            });
            return new MessageCode(1, "入库成功! 数据已经压入队列...");
        }
        return new MessageCode(0, "未知错误: " + logger.toString());
    }

    public void retryDDIB(AllObject allObject) throws Exception {
        int num = allObject.getNum();
        int i = num + 1;
        allObject.setNum(1);
        rabbitProducer.send(allObject, allObject.getObj(), 20);
    }
}


