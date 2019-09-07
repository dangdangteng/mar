package com.dinglicom.mr.producer.confirm;

import com.dinglicom.mr.entity.correlationdata.AllObject;
import com.dinglicom.mr.repository.DecodeFileKidJobRepository;
import com.dinglicom.mr.service.DDiBJobService;
import com.dinglicom.mr.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Log
@Component
public class DDIBConfirm {
    @Autowired
    private DDiBJobService dDiBJobService;
    @Autowired
    private DecodeFileKidJobRepository decodeFileKidJobRepository;


    public void DDIBRetryAndUpdate(String correlationData, boolean ack) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AllObject allObject = mapper.readValue(correlationData, AllObject.class);
        Integer id = StringUtils.subToInt(allObject.getId());
        if (ack) {
            LocalDateTime localDateTime = LocalDateTime.now();
            //如果confirm返回成功 则进行更新
            int i = decodeFileKidJobRepository.updateStartTimeById(2, id, localDateTime);
            log.info("decodefilekidjob table 更新状态: " + i);
        } else {
            log.info("异常补救...");
            if (allObject.getNum()<3){
                try {
                    dDiBJobService.retryDDIB(allObject);
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            }else {
                log.info("无法压入队列,请排查队列状态!");
            }
        }
    }
}