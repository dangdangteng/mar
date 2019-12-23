package com.dinglicom.mr.handle.service;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.core.response.ResponseGenerator;
import com.dinglicom.mr.entity.ReportKidJobEntity;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import com.dinglicom.mr.handle.HandleIndexOf;
import com.dinglicom.mr.repository.ReportKidJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "report2")
public class ReportServiceB implements HandleIndexOf {
    @Autowired
    private ReportKidJobRepository reportKidJobRepository;
    @Autowired
    private CloudUnifyRedisFeign cloudUnifyRedisFeign;

    @Override
    public Response jobDoing(String jsonObj) throws Exception {
        String id = jsonObj.substring(0, jsonObj.indexOf("|"));
        Response stringByKey = cloudUnifyRedisFeign.getStringByKey(jsonObj);
        String redisString = stringByKey.getData().toString();
        ObjectMapper objectMapper = new ObjectMapper();
        ReportKidJobEntity reportKidJobEntity = objectMapper.readValue(redisString, ReportKidJobEntity.class);
        ReportKidJobEntity save = reportKidJobRepository.save(reportKidJobEntity);
        Response<Boolean> remove = cloudUnifyRedisFeign.remove(jsonObj);
        Response<Long> longResponse = cloudUnifyRedisFeign.incrRanNum(id + "IB", 200);
        return ResponseGenerator.genSuccessResult();
    }
}
