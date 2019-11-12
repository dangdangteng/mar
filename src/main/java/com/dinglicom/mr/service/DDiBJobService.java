package com.dinglicom.mr.service;

import com.dingli.ImportDbConfig;
import com.dingli.cloudunify.core.response.Response;
import com.dingli.damain.TaskRequest;
import com.dinglicom.mr.Enum.StatusEnum;
import com.dinglicom.mr.entity.DecodeFile;
import com.dinglicom.mr.entity.DecodeFileKidJob;
import com.dinglicom.mr.entity.TaskConfig;
import com.dinglicom.mr.entity.correlationdata.AllObject;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import com.dinglicom.mr.producer.RabbitProducer;
import com.dinglicom.mr.repository.DecodeFileKidJobRepository;
import com.dinglicom.mr.repository.DecodeFileRepository;
import com.dinglicom.mr.repository.TaskConfigRepository;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.util.TaskUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Optional;

@Log
@Service(value = "DDiBJobService")
public class DDiBJobService {
    private Logger logger = LoggerFactory.getLogger(DDiBJobService.class);
    @Autowired
    private RabbitProducer rabbitProducer;
    @Autowired
    private TaskConfigRepository taskConfigRepository;
    @Autowired
    private DecodeFileKidJobRepository decodeFileKidJobRepository;
    @Autowired
    private DecodeFileRepository decodeFileRepository;
    @Resource
    private CloudUnifyRedisFeign cloudUnifyRedisFeign;

    @Value("${import.url}")
    private String url;

    @Value("${import.datasource}")
    private String datasource;

    @Value("${import.user}")
    private String user;

    @Value("${import.password}")
    private String password;

    @Value("${import.port}")
    private String myport;

    @Value("${import.clientFlag}")
    private String clientFlag;

    public MessageCode ddib(Integer id, String decodeFilePathName, int port, Integer priority) throws Exception {
        Optional<TaskConfig> byId = taskConfigRepository.findById(2);
        DecodeFile decodeFile = new DecodeFile(id, port, decodeFilePathName);
        log.info("DecodeFile :" + decodeFile.getId() + "_" + decodeFile.getFileName() + "_" + decodeFile.getFilePathName()+"_" + decodeFile.getPort());
        TaskRequest taskRequest = TaskUtil.decodeFileToTaskRequest(decodeFile, byId.get());
        log.info("taskRequest :" + taskRequest.toString());
        if (taskRequest == null) {
            return new MessageCode(0, "taskconfig : 配置错误!");
        }
        ArrayList arrayList = new ArrayList();
        ImportDbConfig importDbConfig = new ImportDbConfig();
        importDbConfig.setConfig(byId.get().getConfigPath(), byId.get().getTemplatePath(), byId.get().getConfigPath());
        //TODO: 配置一定要我们传过去吗???
        importDbConfig.setDBConnectionConfig(url, datasource, user, password, myport, String.valueOf(clientFlag));

        log.info("success ----------------"+importDbConfig.toString());
        boolean b = importDbConfig.getImportRequestFile(taskRequest, arrayList);
        log.info("-----------------" + b);
        if (b) {
            arrayList.parallelStream().forEach(o -> {
                try {
                    DecodeFileKidJob decodeFileKidJob = new DecodeFileKidJob();
                    log.info("拆任务并发入库开始...");
                    decodeFileKidJob.setTaskId(id);
                    decodeFileKidJob.setLevel(1);
                    decodeFileKidJob.setRetryCount(0);
                    decodeFileKidJob.setState(1);
                    decodeFileKidJob.setStartTime(System.currentTimeMillis());
                    decodeFileKidJob.setEndTime(null);
                    decodeFileKidJob.setException(StatusEnum.getMessage(1));
                    decodeFileKidJob.setReturnValue(o + "");
                    ObjectMapper objectMapper = new ObjectMapper();
                    String s = objectMapper.writeValueAsString(decodeFileKidJob);
//                    DecodeFileKidJob save = decodeFileKidJobRepository.save(decodeFileKidJob);
                    Response response = cloudUnifyRedisFeign.addString(s, id + "|ddib");
                    log.info("DDIB入库完毕..." + response.toString());
                    AllObject allObject = new AllObject();
                    allObject.setId("DDIB:" + decodeFileKidJob.getId());
                    allObject.setPort(port);
                    allObject.setFilePathName(decodeFilePathName);
                    allObject.setObj(o + ":" + "DDIB|" + response.getData());
                    log.info("开始压入队列");
                    rabbitProducer.send(allObject, o + ":" + "DDIB|" + response.getData().toString(), priority == null ? 10 : priority);
                    log.info("消息压入队列成功...");
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


