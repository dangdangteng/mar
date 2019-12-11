package com.dinglicom.mr.controller.job;

import com.dingli.cloudunify.core.response.Response;
import com.dinglicom.mr.Enum.StatusEnum;
import com.dinglicom.mr.entity.DecodeFileKidJobEntity;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import com.dinglicom.mr.repository.DecodeFileKidJobRepository;
import com.dinglicom.mr.repository.DecodeFileRepository;
import com.dinglicom.mr.response.MessageCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Log
@RestController
@RequestMapping("/job/write/dfkj")
public class JobStatusWriteDecodeFileKidJobTableController {
    @Autowired
    private DecodeFileKidJobRepository decodeFileKidJobRepository;
    @Autowired
    private DecodeFileRepository decodeFileRepository;
    @Autowired
    private CloudUnifyRedisFeign cloudUnifyRedisFeign;

    @RequestMapping(value = "/updateStateAndStartTimeById", method = RequestMethod.POST)
    public MessageCode updateStartTimeById(@RequestParam String key) throws Exception {
        log.info("key is :" + key);
        Response stringByKey = cloudUnifyRedisFeign.getStringByKey(key);
        ObjectMapper objectMapper = new ObjectMapper();
        DecodeFileKidJobEntity decodeFileKidJobEntity1 = objectMapper.readValue(stringByKey.getData().toString(), DecodeFileKidJobEntity.class);
        log.info("**value =" + stringByKey.toString());
        decodeFileKidJobEntity1.setState(3);
        Response response = cloudUnifyRedisFeign.updateDate(key, objectMapper.writeValueAsString(decodeFileKidJobEntity1));
        if (response.getData() == "0"){
            return new MessageCode(0,"更新失败！");
        }
        return new MessageCode(1, "更新成功!");
    }

    @RequestMapping(value = "/updateStateAndEndTimeById", method = RequestMethod.POST)
    public MessageCode updateEndTimeById(@RequestParam String id, @RequestParam int stateCode) throws Exception {
        log.info("JobStatusWriteDecodeFileKidJobTableController.updateEndTimeById() :" + id + ":" + stateCode);
        Response stringByKey = cloudUnifyRedisFeign.getStringByKey(id);
        ObjectMapper objectMapper = new ObjectMapper();
        DecodeFileKidJobEntity decodeFileKidJobEntity1 = objectMapper.readValue(stringByKey.getData().toString(), DecodeFileKidJobEntity.class);
        if (stateCode != 2) {
            decodeFileKidJobEntity1.setState(stateCode);
            decodeFileKidJobEntity1.setEndTime(System.currentTimeMillis());
            DecodeFileKidJobEntity save = decodeFileKidJobRepository.save(decodeFileKidJobEntity1);
            return new MessageCode(0, StatusEnum.getMessage(stateCode));
        }
        Response remove = cloudUnifyRedisFeign.remove(id);
        return new MessageCode(1, "更新成功!");
    }
}
