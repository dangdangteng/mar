package com.dinglicom.mr.handle.service;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.core.response.ResponseGenerator;
import com.dinglicom.mr.entity.ReportKidJobEntity;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import com.dinglicom.mr.handle.HandleIndexOf;
import com.dinglicom.mr.producer.RabbitProducer;
import com.dinglicom.mr.repository.ReportKidJobRepository;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.service.ReportIIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log
@Service(value = "report1")
public class ReportServiceA implements HandleIndexOf {

    @Autowired
    private CloudUnifyRedisFeign cloudUnifyRedisFeign;
    @Autowired
    private ReportKidJobRepository reportKidJobRepository;
    @Autowired
    private RabbitProducer rabbitProducer;
    @Autowired
    private ReportIIService reportIIService;

    @Override
    public Response jobDoing(String jsonObj) throws Exception {
        Response stringByKey = cloudUnifyRedisFeign.getStringByKey(jsonObj);
        ObjectMapper objectMapper = new ObjectMapper();
        ReportKidJobEntity reportKidJobEntity = objectMapper.readValue(stringByKey.getData().toString(), ReportKidJobEntity.class);
        Response remove = cloudUnifyRedisFeign.remove(jsonObj);
        String id = jsonObj.substring(0, jsonObj.indexOf("|"));
        Response<Long> longResponse = cloudUnifyRedisFeign.decrNum(id + "IA");
        if (longResponse.getData() == 0) {
            log.info("1级任务完成" + "2级任务开始...");
            MessageCode messageCode = reportIIService.IIReport(id, reportKidJobEntity.getException());
            return ResponseGenerator.genSuccessResult();
        }
        return ResponseGenerator.genSuccessResult();
    }
}
