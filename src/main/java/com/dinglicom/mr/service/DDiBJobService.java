package com.dinglicom.mr.service;
import com.dingli.ImportDbConfig;
import com.dingli.cloudunify.core.response.Response;
import com.dingli.damain.TaskRequest;
import com.dingli.entity.Consts;
import com.dinglicom.mr.Enum.StatusEnum;
import com.dinglicom.mr.entity.DecodeFileEntity;
import com.dinglicom.mr.entity.DecodeFileKidJobEntity;
import com.dinglicom.mr.entity.TaskConfigEntity;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public MessageCode ddib(DecodeFileEntity decodeFileEntity1, String decodeFilePathName, int port, Integer priority) throws Exception {
        Optional<TaskConfigEntity> byId = taskConfigRepository.findById(2);
        DecodeFileEntity decodeFileEntity = new DecodeFileEntity().setId(decodeFileEntity1.getId()).setFileName(decodeFilePathName).setPort(port).setStartDt(LocalDateTime.now());
        log.info("DecodeFileEntity :" + decodeFileEntity.getId() + "_" + decodeFileEntity.getFileName() + "_" + decodeFileEntity.getFilePathName() + "_" + decodeFileEntity.getPort());
        TaskRequest taskRequest = TaskUtil.decodeFileToTaskRequest(decodeFileEntity, byId.get());
        log.info("taskRequest :" + taskRequest.toString());
        if (taskRequest == null) {
            return new MessageCode(0, "taskconfig : 配置错误!");
        }
        ArrayList arrayList = new ArrayList();
        ImportDbConfig importDbConfig = new ImportDbConfig();
        importDbConfig.setConfig(byId.get().getConfigPath(), byId.get().getTemplatePath(), byId.get().getConfigPath());
        importDbConfig.setDBConnectionConfig(url, datasource, user, password, myport, String.valueOf(clientFlag));
        log.info("success ----------------" + importDbConfig.toString());
        Consts sec = new Consts();
        sec.setAreaId(decodeFileEntity1.getAreaIds());
        sec.setDecodeFileId(decodeFileEntity1.getDeviceId() + "");
        sec.setDeviceId(decodeFileEntity1.getDeviceId() + "");
        sec.setDeviceNetType(decodeFileEntity1.getDeviceNetType());
        sec.setGroupId(decodeFileEntity1.getGroupId() + "");
        sec.setStatType(decodeFileEntity1.getDataType());

        boolean b = importDbConfig.getImportRequestFile(taskRequest, arrayList, sec);

        log.info("-----------------" + b);
        if (b) {
            arrayList.parallelStream().forEach(o -> {
                try {
                    DecodeFileKidJobEntity decodeFileKidJobEntity = new DecodeFileKidJobEntity();
                    log.info("拆任务并发入库开始...");
                    decodeFileKidJobEntity.setTaskId(decodeFileEntity1.getId());
                    decodeFileKidJobEntity.setLevel(1);
                    decodeFileKidJobEntity.setRetryCount(0);
                    decodeFileKidJobEntity.setState(1);
                    decodeFileKidJobEntity.setStartTime(System.currentTimeMillis());
                    decodeFileKidJobEntity.setEndTime(null);
                    decodeFileKidJobEntity.setException(StatusEnum.getMessage(1));
                    decodeFileKidJobEntity.setReturnValue(o + "");
                    ObjectMapper objectMapper = new ObjectMapper();
                    String s = objectMapper.writeValueAsString(decodeFileKidJobEntity);
                    Response response = cloudUnifyRedisFeign.addString(s, decodeFileEntity1.getId()+ "|ddib");
                    log.info("DDIB入库完毕..." + response.toString());
                    AllObject allObject = new AllObject();
                    allObject.setId("DDIB:" + decodeFileKidJobEntity.getId());
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

  public static void main(String[] args) {
  }
}


