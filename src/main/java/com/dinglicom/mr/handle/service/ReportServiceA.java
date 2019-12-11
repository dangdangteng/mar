package com.dinglicom.mr.handle.service;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.core.response.ResponseGenerator;
import com.dinglicom.mr.entity.ReportKidJobEntity;
import com.dinglicom.mr.entity.correlationdata.AllObject;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import com.dinglicom.mr.handle.HandleIndexOf;
import com.dinglicom.mr.producer.RabbitProducer;
import com.dinglicom.mr.repository.ReportKidJobRepository;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.util.SAXxml;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Log
@Service(value = "report1")
public class ReportServiceA implements HandleIndexOf {

    @Autowired
    private CloudUnifyRedisFeign cloudUnifyRedisFeign;
    @Autowired
    private ReportKidJobRepository reportKidJobRepository;
    @Autowired
    private RabbitProducer rabbitProducer;
    @Override
    public Response jobDoing(String jsonObj) throws Exception {
        Response stringByKey = cloudUnifyRedisFeign.getStringByKey(jsonObj);
        ObjectMapper objectMapper = new ObjectMapper();
        ReportKidJobEntity reportKidJobEntity = objectMapper.readValue(stringByKey.getData().toString(), ReportKidJobEntity.class);
        Response remove = cloudUnifyRedisFeign.remove(jsonObj);
        String id = jsonObj.substring(0, jsonObj.indexOf("|"));
        Response stringByKey2 = cloudUnifyRedisFeign.getStringByKey(id + "IA");
        log.info("num :"+stringByKey2.getData().toString());
        //理论上返回应该为一个参数，迭代获取data，转换自减 1
        Response num = cloudUnifyRedisFeign.getNum(id + "IA");
        Integer integer = (Integer)num.getData();
        log.info("new num:"+integer);
        if (integer == 0){
            Response remove1 = cloudUnifyRedisFeign.remove(id + "IA");
            log.info("1级任务完成" + "2级任务开始...");
            Response<Set<String>> response1 = cloudUnifyRedisFeign.searchKey(id + "|report2");
            String s = response1.getData().stream().map(ostr -> {
                log.info(ostr + "----------------------------- 这个为空就是错?");
                Response stringByKey1 = cloudUnifyRedisFeign.getStringByKey(ostr);
                try {
                    ReportKidJobEntity reportKidJobEntity1 = objectMapper.readValue((String) stringByKey1.getData(), ReportKidJobEntity.class);
                    AllObject allObject = new AllObject();
                    allObject.setNum(0);
                    allObject.setObj(reportKidJobEntity1.getReturnValue());
                    allObject.setId("REPORT-IB|" + reportKidJobEntity1.getId());
                    MessageCode messageCode = SAXxml.snoreOppositeMe(reportKidJobEntity1.getReturnValue(), 0.2);
                    if (messageCode.getCode() == 1) {
                        //带key 下发队列,便于返回查找value
                        rabbitProducer.send(allObject, reportKidJobEntity1.getReturnValue() + ":REPORT-IB|" + ostr, 60, true);
                        log.info("2级任务*****************");
                    }
                    return messageCode.getMessage();
                } catch (Exception e) {
                    log.info(e.toString());
                }
                return null;
            }).toString();
        }
        return ResponseGenerator.genSuccessResult();
    }
}
